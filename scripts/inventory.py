#!/usr/bin/env python3
"""scripts/inventory.py -- generate docs/inventory-tables.md, the mechanical
part of the inventory of BSF4ooRexx 850's public surface.

Usage:  python3 scripts/inventory.py            (after scripts/build.sh java)

Reads the imported tree (never changes it) and build/classes (for javap).
For each item it records: where it is defined (file:line), whether the
source carries documentation next to it, whether the reference card
mentions it, and in how many test files of testUnits/ its name appears.

"Appears in tests" is a textual search, an indicator, not a measure of
coverage: a name can appear in a test that does not exercise it, and a
generic name (get, init, ...) appears everywhere. Generic names are
marked "n/a".
"""
import os
import re
import subprocess
import sys
from collections import defaultdict

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
DEV = os.path.join(ROOT, 'bsf4oorexx.dev')
BIN = os.path.join(DEV, 'bin')
SRC_JAVA = os.path.join(DEV, 'source_java')
CC = os.path.join(DEV, 'source_cc', 'BSF4ooRexx.cc')
TESTS = os.path.join(DEV, 'testUnits', 'bsf4rexx')
REFCARD = os.path.join(DEV, 'ReferenceCards', 'BSF4ooRexx', 'refcardBSF4ooRexx.pdf')
CLASSES = os.path.join(ROOT, 'build', 'classes')
OUT = os.path.join(ROOT, 'docs', 'inventory-tables.md')

GENERIC = {'init', 'uninit', 'unknown', 'get', 'set', 'put', 'at', 'run', 'call',
           'string', 'makearray', 'supplier', 'items', 'size', 'equals', 'hashcode',
           'tostring', 'main', 'close', 'value', 'values', 'valueof', 'new', 'exit',
           'say', 'linein', 'lineout', 'charin', 'charout', 'chars', 'entry', 'name',
           'version', 'length', 'count', 'list', 'type', 'data', 'id', 'class', 'clz',
           'object', 'result', 'args', 'message', 'reason', 'start', 'stop'}


def strip_java_comments(text):
    """Blank out /* */ and // comments (not inside string literals), keeping
    line numbers."""
    out, i, n, quote = [], 0, len(text), None
    while i < n:
        c = text[i]
        if quote:
            out.append(c)
            if c == '\\':
                out.append(text[i + 1] if i + 1 < n else ''); i += 2; continue
            if c == quote or c == '\n':
                quote = None
            i += 1; continue
        if text.startswith('/*', i):
            j = text.find('*/', i + 2)
            j = n if j < 0 else j + 2
            out.append(''.join('\n' if ch == '\n' else ' ' for ch in text[i:j])); i = j; continue
        if text.startswith('//', i):
            j = text.find('\n', i)
            j = n if j < 0 else j
            out.append(' ' * (j - i)); i = j; continue
        if c in '"\'':
            quote = c
        out.append(c); i += 1
    return ''.join(out)


def rel(p):
    return os.path.relpath(p, ROOT)


def read(p):
    with open(p, 'rb') as f:
        # a lone CR is not a line break for editors or sed: keep line numbers equal
        return f.read().decode('latin-1').replace('\r\n', '\n').replace('\r', ' ')


# ------------------------------------------------------------------ corpora
def load_tests():
    texts = []
    for d, _, fs in os.walk(TESTS):
        for f in fs:
            if f.lower().endswith(('.testgroup', '.rex', '.rxj', '.cls', '.java', '.rxo')):
                texts.append((os.path.join(d, f), read(os.path.join(d, f)).lower()))
    return texts


TEST_TEXTS = load_tests()


def tests_mentioning(*names, dotted=False):
    """dotted: also count the name after a dot (bsf.NAME, ~bsf.NAME)"""
    ns = [n.lower().strip('"\'') for n in names]
    if ns[0] in GENERIC or len(ns[0]) < 3:
        return 'n/a'
    pat = re.compile((r'(?<![\w])' if dotted else r'(?<![\w.])') + '('
                     + '|'.join(re.escape(n) for n in ns) + r')(?![\w])')
    return str(sum(1 for _, t in TEST_TEXTS if pat.search(t)))


def load_refcard():
    try:
        return subprocess.run(['pdftotext', '-layout', REFCARD, '-'], capture_output=True,
                              text=True, check=True).stdout.lower()
    except Exception:
        return None


REFCARD_TEXT = load_refcard()


def on_refcard(name):
    if REFCARD_TEXT is None:
        return '?'
    n = name.lower().strip('"\'')
    if n in GENERIC:
        return 'n/a'
    return 'yes' if re.search(r'(?<![\w])' + re.escape(n) + r'(?![\w])', REFCARD_TEXT) else '-'


# ------------------------------------------------------------------ Rexx
def strip_rexx_comments(text):
    """Blank out /* */ (nested) and -- comments, keeping line numbers.
    Returns (code, comment_mask) where comment_mask[i] is the comment text
    removed from line i (0-based)."""
    out = []
    i, n, depth, quote = 0, len(text), 0, None
    while i < n:
        c = text[i]
        if depth:
            if text.startswith('/*', i):
                depth += 1; out.append('  '); i += 2; continue
            if text.startswith('*/', i):
                depth -= 1; out.append('  '); i += 2; continue
            out.append('\n' if c == '\n' else ' '); i += 1; continue
        if quote:
            out.append(c)
            if c == quote or c == '\n':
                quote = None
            i += 1; continue
        if text.startswith('/*', i):
            depth = 1; out.append('  '); i += 2; continue
        if text.startswith('--', i):
            j = text.find('\n', i)
            j = n if j < 0 else j
            out.append(' ' * (j - i)); i = j; continue
        if c in '"\'':
            quote = c
        out.append(c); i += 1
    return ''.join(out)


DIRECTIVE = re.compile(r'^\s*::\s*(\w+)\s*(.*)$', re.I)


def tokens(s):
    return re.findall(r'"[^"]*"|\'[^\']*\'|\S+', s)


def doc_before(lines, idx):
    """'doc' if a /** ... */ block ends right before line idx (0-based),
    'comment' if another comment does, '-' otherwise."""
    j = idx - 1
    while j >= 0 and lines[j].strip() == '':
        j -= 1
    if j < 0:
        return '-'
    s = lines[j].strip()
    if s.endswith('*/'):
        k = j
        while k >= 0 and '/*' not in lines[k]:
            k -= 1
        if k >= 0 and '/**' in lines[k] and not lines[k].strip().startswith('/***'):
            return 'doc'
        return 'comment'
    if s.startswith('--'):
        return 'comment'
    return '-'


def parse_rexx(path):
    raw = read(path)
    rawlines = raw.split('\n')
    code = strip_rexx_comments(raw).split('\n')
    items = []        # dicts: kind, name, cls, flags, line, doc
    env = []          # (name, line, how)
    cur_cls = None
    for i, line in enumerate(code):
        m = DIRECTIVE.match(line)
        if m:
            kw, rest = m.group(1).lower(), m.group(2)
            t = tokens(rest)
            name = t[0].strip('"\'') if t else ''
            flags = [x.lower() for x in t[1:]]
            if kw == 'class':
                cur_cls = name
                items.append(dict(kind='class', name=name, cls=None, flags=flags, line=i + 1,
                                  doc=doc_before(rawlines, i)))
            elif kw in ('method', 'attribute', 'constant'):
                items.append(dict(kind=kw, name=name, cls=cur_cls, flags=flags, line=i + 1,
                                  doc=doc_before(rawlines, i)))
            elif kw == 'routine':
                cur_cls = None
                ext = re.search(r'external\s+(["\'])(.*?)\1', rest, re.I)
                items.append(dict(kind='routine', name=name, cls=None, flags=flags, line=i + 1,
                                  doc=doc_before(rawlines, i),
                                  external=ext.group(2).split() if ext else None))
            elif kw == 'requires':
                items.append(dict(kind='requires', name=name, cls=None, flags=flags, line=i + 1,
                                  doc='-'))
            continue
        for mm in re.finditer(r'\.(local|environment)\s*~\s*([\w.!?]+)\s*=(?!=)', line, re.I):
            env.append((mm.group(2), i + 1, '.%s~%s=' % (mm.group(1).lower(), mm.group(2))))
        for mm in re.finditer(r'\.(local|environment)\s*~\s*(?:put|setentry)\s*\(\s*(.*?)\)', line, re.I):
            args = mm.group(2)
            q = re.findall(r'["\']([^"\']+)["\']', args)
            nm = q[-1] if 'put' in mm.group(0).lower() and q else (q[0] if q else args)
            env.append((nm, i + 1, '.%s~%s' % (mm.group(1).lower(),
                                               'put' if 'put' in mm.group(0).lower() else 'setEntry')))
    return items, env


def is_public(it, classes):
    if it['kind'] == 'class':
        return 'public' in it['flags']
    if it['kind'] == 'routine':
        return 'public' in it['flags']
    if it['kind'] in ('method', 'attribute', 'constant'):
        c = classes.get((it['cls'] or '').lower())
        return c is not None and 'public' in c['flags'] and 'private' not in it['flags']
    return False


def rexx_section(out):
    pkgs = sorted([f for f in os.listdir(BIN)
                   if f.lower().endswith(('.cls', '.rex', '.rxo', '.rxj'))], key=str.lower)
    out.append('## 1. Rexx packages (`bsf4oorexx.dev/bin/`)\n')
    out.append('Columns: **Line** in the package; **Src** = `doc` if an ooRexxDoc comment '
               '(`/** ... */`) precedes the directive, `comment` if another comment does, '
               '`-` if none; **Refcard** = named on `refcardBSF4ooRexx.pdf`; **Tests** = number '
               'of files under `testUnits/bsf4rexx/` where the name appears.\n')
    out.append('Only the public surface is listed: public classes and their non-private methods, '
               'attributes and constants; public routines. Private items are counted.\n')
    summary = []
    for p in pkgs:
        path = os.path.join(BIN, p)
        items, env = parse_rexx(path)
        classes = {it['name'].lower(): it for it in items if it['kind'] == 'class'}
        pub = [it for it in items if is_public(it, classes)]
        priv = [it for it in items if it['kind'] in ('class', 'method', 'attribute', 'constant', 'routine')
                and not is_public(it, classes)]
        reqs = [it for it in items if it['kind'] == 'requires']
        summary.append((p, len(pub), len(priv), len({(n.lower(), h) for n, _, h in env})))
        out.append('\n### %s\n' % p)
        out.append('`%s`; %d public items, %d non-public. Requires: %s.\n' % (
            rel(path), len(pub), len(priv),
            ', '.join('`%s`%s (line %d)' % (r['name'], ' library' if 'library' in r['flags'] else '', r['line'])
                      for r in reqs) or 'nothing'))
        if pub:
            out.append('| Item | Kind | Flags | Line | Src | Refcard | Tests |')
            out.append('|---|---|---|---:|---|---|---:|')
            for it in pub:
                nm = it['name'] if it['kind'] in ('class', 'routine') else '%s~%s' % (it['cls'], it['name'])
                flags = [f for f in it['flags'] if f not in ('public',)]
                if it['kind'] == 'routine' and it.get('external'):
                    flags = ['external: `%s`' % ' '.join(it['external'])]
                out.append('| `%s` | %s | %s | %d | %s | %s | %s |' % (
                    nm, it['kind'], ' '.join(flags).replace('|', '\\|'), it['line'], it['doc'],
                    on_refcard(it['name']), tests_mentioning(it['name'])))
        if env:
            out.append('\nEnvironment entries set by this package:\n')
            out.append('| Entry | Line | How |')
            out.append('|---|---:|---|')
            seen = set()
            for nm, ln, how in env:
                if (nm.lower(), how) in seen:
                    continue
                seen.add((nm.lower(), how))
                out.append('| `%s` | %d | `%s` |' % (nm, ln, how))
    return summary


# ------------------------------------------------------------------ BSF() subfunctions
def subfunctions_section(out):
    it_path = os.path.join(SRC_JAVA, 'InvocationType.java')
    raj_path = os.path.join(SRC_JAVA, 'RexxAndJava.java')
    it_lines = read(it_path).split('\n')
    raj_lines = read(raj_path).split('\n')
    enum = []
    for i, l in enumerate(it_lines):
        m = re.match(r'\s*([A-Z_]+)\s*\(\s*"(\w+)"\s*\)\s*[,;]', l)
        if m:
            enum.append((m.group(1), m.group(2), i + 1))
    syn = defaultdict(list)
    for l in it_lines:
        m = re.search(r'upperCase2InvocationType\.put\(\s*"(\w+)"\s*,\s*([A-Z_]+)', l)
        if m and m.group(1) != m.group(2):
            syn[m.group(2)].append(m.group(1))
    case_line = {}
    for i, l in enumerate(raj_lines):
        for m in re.finditer(r'\bcase\s+([A-Z_]+)\s*:', l):
            case_line.setdefault(m.group(1), i + 1)
    bsfcls = read(os.path.join(BIN, 'BSF.CLS')).lower()
    out.append('\n## 2. Subfunctions of the external function `BSF()`\n')
    out.append('The native routine `BSF` passes its first argument to '
               '`RexxAndJava.javaCallBSF()` (`%s`), which looks it up, case-insensitively, in the '
               'enum `InvocationType` (`%s`). %d subfunctions. **Case** = line of the `case` in '
               '`RexxAndJava.java`; **BSF.CLS** = whether `BSF.CLS` uses the name in quotes; '
               '**Tests**: files where the name or a synonym appears, alone or after a dot '
               '(`bsf.createJavaArray`, the `BSF.CLS` wrappers).\n' % (rel(raj_path), rel(it_path), len(enum)))
    out.append('| Subfunction | Synonyms | Enum (line) | Case | BSF.CLS | Refcard | Tests |')
    out.append('|---|---|---|---:|---|---|---:|')
    for e, name, ln in enum:
        syns = [s for s in syn.get(e, []) if s != name.upper()]
        used = 'yes' if re.search(r'["\']' + re.escape(name.lower()) + r'["\']', bsfcls) else '-'
        out.append('| `%s` | %s | `%s` (%d) | %s | %s | %s | %s |' % (
            name, ', '.join('`%s`' % s for s in syns) or '', e, ln,
            case_line.get(e, '-'), used, on_refcard(name), tests_mentioning(name, *syns, dotted=True)))
    return len(enum)


# ------------------------------------------------------------------ native
def native_section(out):
    cc = read(CC).split('\n')
    table = []
    cond = []
    in_table = False
    for i, l in enumerate(cc):
        if 'RexxRoutineEntry bsf_external_functions[]' in l:
            in_table = True; continue
        if in_table:
            if 'REXX_LAST_ROUTINE' in l:
                break
            st = l.strip()
            if re.match(r'#\s*if', st):
                cond.append(re.sub(r'\s*//.*$', '', st))
            elif re.match(r'#\s*endif', st) and cond:
                cond.pop()
            s = l.split('//')[0]
            m = re.search(r'REXX_TYPED_ROUTINE\(\s*(\w+)', s)
            if m and not st.startswith('//'):
                table.append((m.group(1), i + 1, ' && '.join(cond)))
    defs = {}
    for i, l in enumerate(cc):
        m = re.match(r'\s*RexxRoutine\d\s*\(\s*[\w ]+,\s*(\w+)', l)
        if m:
            defs.setdefault(m.group(1), i + 1)
    # which BSF.CLS routines map to which native routine
    ext = {}
    for p in os.listdir(BIN):
        if not p.lower().endswith(('.cls', '.rex')):
            continue
        items, _ = parse_rexx(os.path.join(BIN, p))
        for it in items:
            if it['kind'] == 'routine' and it.get('external'):
                e = it['external']
                if len(e) >= 3 and e[0].lower() == 'library':
                    ext.setdefault(e[2].lower(), []).append('`%s` %s:%d' % (it['name'], p, it['line']))
    out.append('\n## 3. The JNI library (`libBSF4ooRexx850.so`, `%s`)\n' % rel(CC))
    out.append('### 3.1 Rexx external routines\n')
    out.append('Registered in the package table `bsf_external_functions[]`; `::requires '
               '"BSF4ooRexx850" library` (BSF.CLS) makes them available. **Table** = line in '
               'the table; **Defined** = line of the `RexxRoutineN(...)`; **Only if** = the '
               'preprocessor condition around the table entry.\n')
    out.append('| Routine | Table | Defined | Only if | Wrapped by | Refcard | Tests |')
    out.append('|---|---:|---:|---|---|---|---:|')
    for nm, ln, cnd in table:
        out.append('| `%s` | %d | %s | %s | %s | %s | %s |' % (
            nm, ln, defs.get(nm, '-'), ('`%s`' % cnd) if cnd else '',
            ', '.join(ext.get(nm.lower(), [])) or '', on_refcard(nm), tests_mentioning(nm)))
    # JNI functions
    jni = []
    for i, l in enumerate(cc):
        m = re.match(r'\s*JNIEXPORT\s+\w+\s+JNICALL\s+(Java_\w+)', l)
        if m:
            jni.append((m.group(1), i + 1))
    natives = {}
    for f in ('RexxAndJava.java', 'RexxCleanupRef.java'):
        for i, l in enumerate(strip_java_comments(read(os.path.join(SRC_JAVA, f))).split('\n')):
            m = re.search(r'\bnative\b[^(]*?\b(\w+)\s*\(', l)
            if m:
                natives.setdefault((f[:-5], m.group(1)), []).append(i + 1)
    out.append('\n### 3.2 JNI functions (called by the Java side)\n')
    out.append('| Java class | Native method | `.cc` line | Java declaration |')
    out.append('|---|---|---:|---|')
    seen = set()
    for nm, ln in jni:
        m = re.match(r'Java_org_rexxla_bsf_engines_rexx_(\w+?)_(jni\w+?)(?:__.*)?$', nm)
        if not m:
            out.append('| ? | `%s` | %d | |' % (nm, ln)); continue
        cls, meth = m.group(1), m.group(2).replace('_1', '_')
        key = (cls, meth, ln)
        if key in seen:
            continue
        seen.add(key)
        decl = natives.get((cls, meth))
        out.append('| %s | `%s` | %d | %s |' % (cls, meth, ln,
                   ('%s.java:%s' % (cls, ','.join(map(str, decl)))) if decl else '**not declared**'))
    undeclared = [k for k in natives if not any(k[0] == c and k[1] == m for (c, m, _) in seen)]
    if undeclared:
        out.append('\nDeclared `native` in Java without a matching `JNIEXPORT` in the `.cc`: ' +
                   ', '.join('`%s.%s`' % k for k in sorted(undeclared)) + '.')
    return len(table), len(seen)


# ------------------------------------------------------------------ Java
def javap(classnames):
    r = subprocess.run(['javap', '-protected', '-cp', CLASSES] + classnames, capture_output=True,
                       text=True, env=dict(os.environ, JAVA_TOOL_OPTIONS=''))
    return r.stdout


def java_sources():
    """map fully-qualified top-level class name -> source path"""
    m = {}
    for d in (SRC_JAVA, ROOT):
        for dirpath, _, fs in os.walk(d if d == SRC_JAVA else os.path.join(ROOT, 'org')):
            for f in fs:
                if f.endswith('.java'):
                    p = os.path.join(dirpath, f)
                    txt = read(p)
                    pk = re.search(r'^\s*package\s+([\w.]+)\s*;', txt, re.M)
                    fq = (pk.group(1) + '.' if pk else '') + f[:-5]
                    if fq not in m or d == SRC_JAVA:      # source_java is the master
                        m[fq] = p
    return m


def javadoc_before(lines, idx):
    j = idx - 1
    while j >= 0 and (lines[j].strip() == '' or lines[j].strip().startswith('@')):
        j -= 1
    if j >= 0 and lines[j].strip().endswith('*/'):
        k = j
        while k >= 0 and '/*' not in lines[k]:
            k -= 1
        return 'yes' if k >= 0 and '/**' in lines[k] else '-'
    return '-'


def java_section(out):
    if not os.path.isdir(CLASSES):
        out.append('\n## 4. Java\n\n(build/classes missing: run scripts/build.sh java)\n')
        return 0, 0
    srcs = java_sources()
    allcls = []
    for dirpath, _, fs in os.walk(CLASSES):
        for f in fs:
            if f.endswith('.class'):
                allcls.append(os.path.relpath(os.path.join(dirpath, f), CLASSES)[:-6].replace('/', '.'))
    own = sorted(c for c in allcls if c.startswith(('org.rexxla.', 'org.oorexx.'))
                 and not re.search(r'\$\d', c) and not c.startswith('org.oorexx.datergf.test'))
    bsf = sorted(c for c in allcls if c.startswith('org.apache.bsf.') and not re.search(r'\$\d', c)
                 and '.test.' not in c)
    text = javap(own)
    blocks = re.split(r'\n(?=(?:Compiled from|[a-z][\w ]*(?:class|interface|enum) ))', text)
    out.append('\n## 4. Java\n')
    out.append('### 4.1 BSF4ooRexx classes (`org.rexxla.*`, `org.oorexx.*`)\n')
    out.append('Public and protected members of public classes (javap -protected on the build). '
               '**Line** = declaration in the source (`bsf4oorexx.dev/source_java/` is the master '
               'for `org.rexxla.*`); **Javadoc** = a `/** */` comment precedes it; **Tests** = files '
               'under `testUnits/bsf4rexx/` where the member name appears.\n')
    ncls = nmem = 0
    for b in blocks:
        lines = [l for l in b.split('\n') if l.strip() and not l.startswith('Compiled from')]
        if not lines:
            continue
        head = lines[0]
        m = re.match(r'^([\w ]*?)(class|interface|enum) ([\w.$]+)', head)
        if not m or 'public' not in m.group(1):
            continue
        fq = m.group(3)
        top = fq.split('$')[0]
        src = srcs.get(top)
        slines = read(src).split('\n') if src else []
        clines = strip_java_comments(read(src)).split('\n') if src else []
        simple = fq.split('.')[-1].split('$')[-1]
        cl = None
        for i, l in enumerate(slines):
            if re.search(r'\b(class|interface|enum)\s+' + re.escape(simple) + r'\b', clines[i]):
                cl = i + 1; break
        ncls += 1
        out.append('\n#### `%s` (%s)\n' % (fq, m.group(2)))
        out.append('Source: `%s`%s; class Javadoc: %s.\n' % (
            rel(src) if src else '?', (':%d' % cl) if cl else '',
            javadoc_before(slines, cl - 1) if cl else '?'))
        rows = []
        used = defaultdict(int)
        for l in lines[1:]:
            l = l.strip().rstrip(';')
            if l in ('}', '{') or l.startswith('static {}'):
                continue
            mm = re.search(r'([\w$]+)\s*\(', l)
            if mm:
                name = mm.group(1)
                kind = 'constructor' if name == fq.split('.')[-1] else 'method'
                name = simple if kind == 'constructor' else name
                pat = re.compile(r'^\s*(?:(?:public|protected|static|final|synchronized|abstract|native|default|strictfp)\s+)*'
                                 r'(?:<[^>]+>\s+)?(?:[\w.$<>\[\], ?]+\s+)?' + re.escape(name) + r'\s*\(')
            else:
                name = l.split()[-1]
                kind = 'field'
                pat = re.compile(r'^\s*(?:(?:public|protected|static|final|transient|volatile)\s+)+[\w.$<>\[\], ?]+\s+'
                                 + re.escape(name) + r'\s*(=|;|,)')
            if '.' in name and kind != 'constructor':
                continue
            hits = [i for i, s in enumerate(clines) if pat.search(s)]
            if not hits and kind == 'field':      # enum constants, C-style arrays
                alt = re.compile(r'^\s*' + re.escape(name) + r'\s*[(,;{]|\b' + re.escape(name)
                                 + r'\s*\[\s*\]\s*(=|;|,)')
                hits = [i for i, s in enumerate(clines) if alt.search(s)]
            if not hits and kind == 'field':      # several constants on one line
                alt = re.compile(r'\b' + re.escape(name) + r'\b')
                hits = [i for i, s in enumerate(clines) if alt.search(s)][:1]
            k = used[(kind, name)]
            used[(kind, name)] += 1
            ln = hits[k] if k < len(hits) else (hits[0] if hits else None)
            jd = javadoc_before(slines, ln) if ln is not None else '?'
            if ln is None and (kind == 'constructor' or name in ('values', 'valueOf')):
                jd = 'implicit'
            sig = re.sub(r'\b(java\.lang|java\.util|org\.rexxla\.bsf\.engines\.rexx|org\.apache\.bsf)\.', '', l)
            rows.append('| `%s` | %s | %s | %s | %s |' % (sig.replace('|', '\\|'), kind,
                        ln + 1 if ln is not None else ('-' if jd == 'implicit' else '?'), jd,
                        tests_mentioning(name) if kind != 'constructor' else tests_mentioning(simple)))
            nmem += 1
        if rows:
            out.append('| Member | Kind | Line | Javadoc | Tests |')
            out.append('|---|---|---:|---|---:|')
            out.extend(rows)
    # Apache BSF: class level
    out.append('\n### 4.2 Apache BSF 2.4 as carried by BSF4ooRexx (`org.apache.bsf.*`)\n')
    out.append('Rony\'s copy of Apache BSF 2.4 (the `BSFManager` API through which Java programs '
               'can run Rexx scripts, and which the bridge uses internally). Class level only: '
               'number of public/protected members, from javap.\n')
    out.append('| Class | Source | Members |')
    out.append('|---|---|---:|')
    t2 = javap(bsf)
    for b in re.split(r'\n(?=(?:Compiled from|[a-z][\w ]*(?:class|interface|enum) ))', t2):
        lines = [l for l in b.split('\n') if l.strip() and not l.startswith('Compiled from')]
        if not lines:
            continue
        m = re.match(r'^([\w ]*?)(class|interface|enum) ([\w.$]+)', lines[0])
        if not m or 'public' not in m.group(1):
            continue
        fq = m.group(3)
        src = srcs.get(fq.split('$')[0])
        out.append('| `%s` | `%s` | %d |' % (fq, rel(src) if src else '-',
                   len([l for l in lines[1:] if l.strip() not in ('}', '{')])))
    return ncls, nmem


def mains_section(out):
    out.append('\n### 4.3 Classes with a `main()` method (Java-side entry points)\n')
    out.append('| Class | Source line |')
    out.append('|---|---|')
    srcs = java_sources()
    for fq, p in sorted(srcs.items()):
        if not fq.startswith(('org.rexxla.', 'org.oorexx.', 'org.apache.bsf.')):
            continue
        for i, l in enumerate(read(p).split('\n')):
            if re.search(r'public\s+static\s+void\s+main\s*\(', l) or re.search(r'static\s+public\s+void\s+main\s*\(', l):
                out.append('| `%s` | `%s:%d` |' % (fq, rel(p), i + 1))
                break


def main():
    out = ['# BSF4ooRexx 850: inventory tables', '',
           '*Generated by `scripts/inventory.py` from the tree at this commit; do not edit '
           'by hand. The overview and the method are in [`inventory.md`](inventory.md).*', '']
    s = rexx_section(out)
    nsub = subfunctions_section(out)
    nroutines, njni = native_section(out)
    ncls, nmem = java_section(out)
    mains_section(out)
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w') as f:
        f.write('\n'.join(out) + '\n')
    print('wrote', rel(OUT))
    for p, a, b, e in s:
        print('  %-32s public=%3d non-public=%3d env=%d' % (p, a, b, e))
    print('  BSF() subfunctions:', nsub)
    print('  native Rexx routines:', nroutines, ' JNI functions:', njni)
    print('  Java (own) public classes:', ncls, ' members:', nmem)


if __name__ == '__main__':
    main()
