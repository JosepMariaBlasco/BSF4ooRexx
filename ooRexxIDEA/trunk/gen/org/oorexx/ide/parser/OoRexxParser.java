// This is a generated file. Not intended for manual editing.
package org.oorexx.ide.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.oorexx.ide.lang.OoRexxTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static org.oorexx.ide.intellij.util.ResourceDelimiterParser.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class OoRexxParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ARRAY_TERM) {
      r = array_term(b, 0);
    }
    else if (t == ARRAY_TERM_PARENTHIZED) {
      r = array_term_parenthized(b, 0);
    }
    else if (t == ASSIGNMENT) {
      r = assignment(b, 0);
    }
    else if (t == BOOL_LIT) {
      r = bool_lit(b, 0);
    }
    else if (t == BUILT_IN_ABBREV) {
      r = built_in_abbrev(b, 0);
    }
    else if (t == BUILT_IN_ABS) {
      r = built_in_abs(b, 0);
    }
    else if (t == BUILT_IN_ADDRESS) {
      r = built_in_address(b, 0);
    }
    else if (t == BUILT_IN_ARG) {
      r = built_in_arg(b, 0);
    }
    else if (t == BUILT_IN_B_2_X) {
      r = built_in_b2x(b, 0);
    }
    else if (t == BUILT_IN_BEEP) {
      r = built_in_beep(b, 0);
    }
    else if (t == BUILT_IN_BITAND) {
      r = built_in_bitand(b, 0);
    }
    else if (t == BUILT_IN_BITOR) {
      r = built_in_bitor(b, 0);
    }
    else if (t == BUILT_IN_BITXOR) {
      r = built_in_bitxor(b, 0);
    }
    else if (t == BUILT_IN_C_2_D) {
      r = built_in_c2d(b, 0);
    }
    else if (t == BUILT_IN_C_2_X) {
      r = built_in_c2x(b, 0);
    }
    else if (t == BUILT_IN_CENTER) {
      r = built_in_center(b, 0);
    }
    else if (t == BUILT_IN_CHANGESTR) {
      r = built_in_changestr(b, 0);
    }
    else if (t == BUILT_IN_CHARIN) {
      r = built_in_charin(b, 0);
    }
    else if (t == BUILT_IN_CHAROUT) {
      r = built_in_charout(b, 0);
    }
    else if (t == BUILT_IN_CHARS) {
      r = built_in_chars(b, 0);
    }
    else if (t == BUILT_IN_COMPARE) {
      r = built_in_compare(b, 0);
    }
    else if (t == BUILT_IN_CONDITION) {
      r = built_in_condition(b, 0);
    }
    else if (t == BUILT_IN_COPIES) {
      r = built_in_copies(b, 0);
    }
    else if (t == BUILT_IN_COUNTSTR) {
      r = built_in_countstr(b, 0);
    }
    else if (t == BUILT_IN_D_2_C) {
      r = built_in_d2c(b, 0);
    }
    else if (t == BUILT_IN_D_2_X) {
      r = built_in_d2x(b, 0);
    }
    else if (t == BUILT_IN_DATATYPE) {
      r = built_in_datatype(b, 0);
    }
    else if (t == BUILT_IN_DATE) {
      r = built_in_date(b, 0);
    }
    else if (t == BUILT_IN_DELSTR) {
      r = built_in_delstr(b, 0);
    }
    else if (t == BUILT_IN_DELWORD) {
      r = built_in_delword(b, 0);
    }
    else if (t == BUILT_IN_DIGITS) {
      r = built_in_digits(b, 0);
    }
    else if (t == BUILT_IN_DIRECTORY) {
      r = built_in_directory(b, 0);
    }
    else if (t == BUILT_IN_ENDLOCAL) {
      r = built_in_endlocal(b, 0);
    }
    else if (t == BUILT_IN_ERRORTEXT) {
      r = built_in_errortext(b, 0);
    }
    else if (t == BUILT_IN_FILESPEC) {
      r = built_in_filespec(b, 0);
    }
    else if (t == BUILT_IN_FORM) {
      r = built_in_form(b, 0);
    }
    else if (t == BUILT_IN_FORMAT) {
      r = built_in_format(b, 0);
    }
    else if (t == BUILT_IN_FUNCTIONS_KEYWORD_LIST) {
      r = built_in_functions_keyword_list(b, 0);
    }
    else if (t == BUILT_IN_FUZZ) {
      r = built_in_fuzz(b, 0);
    }
    else if (t == BUILT_IN_INSERT) {
      r = built_in_insert(b, 0);
    }
    else if (t == BUILT_IN_LASTPOS) {
      r = built_in_lastpos(b, 0);
    }
    else if (t == BUILT_IN_LEFT) {
      r = built_in_left(b, 0);
    }
    else if (t == BUILT_IN_LENGTH) {
      r = built_in_length(b, 0);
    }
    else if (t == BUILT_IN_LINEIN) {
      r = built_in_linein(b, 0);
    }
    else if (t == BUILT_IN_LINEOUT) {
      r = built_in_lineout(b, 0);
    }
    else if (t == BUILT_IN_LINES) {
      r = built_in_lines(b, 0);
    }
    else if (t == BUILT_IN_LOWER) {
      r = built_in_lower(b, 0);
    }
    else if (t == BUILT_IN_MAX) {
      r = built_in_max(b, 0);
    }
    else if (t == BUILT_IN_MIN) {
      r = built_in_min(b, 0);
    }
    else if (t == BUILT_IN_OVERLAY) {
      r = built_in_overlay(b, 0);
    }
    else if (t == BUILT_IN_POS) {
      r = built_in_pos(b, 0);
    }
    else if (t == BUILT_IN_QUALIFY) {
      r = built_in_qualify(b, 0);
    }
    else if (t == BUILT_IN_QUEUED) {
      r = built_in_queued(b, 0);
    }
    else if (t == BUILT_IN_RANDOM) {
      r = built_in_random(b, 0);
    }
    else if (t == BUILT_IN_REVERSE) {
      r = built_in_reverse(b, 0);
    }
    else if (t == BUILT_IN_RIGHT) {
      r = built_in_right(b, 0);
    }
    else if (t == BUILT_IN_RXFUNCADD) {
      r = built_in_rxfuncadd(b, 0);
    }
    else if (t == BUILT_IN_RXFUNCDROP) {
      r = built_in_rxfuncdrop(b, 0);
    }
    else if (t == BUILT_IN_RXFUNCQUERY) {
      r = built_in_rxfuncquery(b, 0);
    }
    else if (t == BUILT_IN_RXQUEUE) {
      r = built_in_rxqueue(b, 0);
    }
    else if (t == BUILT_IN_SETLOCAL) {
      r = built_in_setlocal(b, 0);
    }
    else if (t == BUILT_IN_SIGN) {
      r = built_in_sign(b, 0);
    }
    else if (t == BUILT_IN_SOURCELINE) {
      r = built_in_sourceline(b, 0);
    }
    else if (t == BUILT_IN_SPACE) {
      r = built_in_space(b, 0);
    }
    else if (t == BUILT_IN_STREAM) {
      r = built_in_stream(b, 0);
    }
    else if (t == BUILT_IN_STRIP) {
      r = built_in_strip(b, 0);
    }
    else if (t == BUILT_IN_SUBSTR) {
      r = built_in_substr(b, 0);
    }
    else if (t == BUILT_IN_SUBWORD) {
      r = built_in_subword(b, 0);
    }
    else if (t == BUILT_IN_SYMBOL) {
      r = built_in_symbol(b, 0);
    }
    else if (t == BUILT_IN_TIME) {
      r = built_in_time(b, 0);
    }
    else if (t == BUILT_IN_TRACE) {
      r = built_in_trace(b, 0);
    }
    else if (t == BUILT_IN_TRANSLATE) {
      r = built_in_translate(b, 0);
    }
    else if (t == BUILT_IN_TRUNC) {
      r = built_in_trunc(b, 0);
    }
    else if (t == BUILT_IN_UPPER) {
      r = built_in_upper(b, 0);
    }
    else if (t == BUILT_IN_USERID) {
      r = built_in_userid(b, 0);
    }
    else if (t == BUILT_IN_VALUE) {
      r = built_in_value(b, 0);
    }
    else if (t == BUILT_IN_VAR) {
      r = built_in_var(b, 0);
    }
    else if (t == BUILT_IN_VERIFY) {
      r = built_in_verify(b, 0);
    }
    else if (t == BUILT_IN_WORD) {
      r = built_in_word(b, 0);
    }
    else if (t == BUILT_IN_WORDINDEX) {
      r = built_in_wordindex(b, 0);
    }
    else if (t == BUILT_IN_WORDLENGTH) {
      r = built_in_wordlength(b, 0);
    }
    else if (t == BUILT_IN_WORDPOS) {
      r = built_in_wordpos(b, 0);
    }
    else if (t == BUILT_IN_WORDS) {
      r = built_in_words(b, 0);
    }
    else if (t == BUILT_IN_X_2_B) {
      r = built_in_x2b(b, 0);
    }
    else if (t == BUILT_IN_X_2_C) {
      r = built_in_x2c(b, 0);
    }
    else if (t == BUILT_IN_X_2_D) {
      r = built_in_x2d(b, 0);
    }
    else if (t == BUILT_IN_XRANGE) {
      r = built_in_xrange(b, 0);
    }
    else if (t == COMMA_SEPARATED_EXPR) {
      r = comma_separated_expr(b, 0);
    }
    else if (t == DIRECTIVE) {
      r = directive(b, 0);
    }
    else if (t == DIRECTIVE_ACCESS) {
      r = directive_access(b, 0);
    }
    else if (t == DIRECTIVE_ACCESS_PRIVATE) {
      r = directive_access_private(b, 0);
    }
    else if (t == DIRECTIVE_ACCESS_PUBLIC) {
      r = directive_access_public(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE) {
      r = directive_annotate(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_ATTRIBUTE) {
      r = directive_annotate_attribute(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_CLASS) {
      r = directive_annotate_class(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_CONSTANT) {
      r = directive_annotate_constant(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_DATA) {
      r = directive_annotate_data(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_METHOD) {
      r = directive_annotate_method(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_NAME) {
      r = directive_annotate_name(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_PACKAGE) {
      r = directive_annotate_package(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_REFERENCE) {
      r = directive_annotate_reference(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_REFERENCE_NAME) {
      r = directive_annotate_reference_name(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_ROUTINE) {
      r = directive_annotate_routine(b, 0);
    }
    else if (t == DIRECTIVE_ANNOTATE_VALUE) {
      r = directive_annotate_value(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE) {
      r = directive_attribute(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_ABSTRACT) {
      r = directive_attribute_abstract(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_ABSTRACT_DELEGATE_EXTERNAL) {
      r = directive_attribute_abstract_delegate_external(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_ACCESS) {
      r = directive_attribute_access(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_BODY) {
      r = directive_attribute_body(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_CLASS) {
      r = directive_attribute_class(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_DELEGATE) {
      r = directive_attribute_delegate(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_EXTERNAL) {
      r = directive_attribute_external(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_GET_SET) {
      r = directive_attribute_get_set(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_GUARD_UNGUARDED) {
      r = directive_attribute_guard_unguarded(b, 0);
    }
    else if (t == DIRECTIVE_ATTRIBUTE_UNPROTECTED_PROTECTED) {
      r = directive_attribute_unprotected_protected(b, 0);
    }
    else if (t == DIRECTIVE_CLASS) {
      r = directive_class(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_ABSTRACT) {
      r = directive_class_abstract(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_ACCESS) {
      r = directive_class_access(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_BODY) {
      r = directive_class_body(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_INHERIT) {
      r = directive_class_inherit(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_INHERIT_NAME) {
      r = directive_class_inherit_name(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_METACLASS) {
      r = directive_class_metaclass(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_METACLASS_NAME) {
      r = directive_class_metaclass_name(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_MIXINCLASS) {
      r = directive_class_mixinclass(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_MIXINCLASS_NAME) {
      r = directive_class_mixinclass_name(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_MIXINCLASS_SUBCLASS) {
      r = directive_class_mixinclass_subclass(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_NAME) {
      r = directive_class_name(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_SUBCLASS) {
      r = directive_class_subclass(b, 0);
    }
    else if (t == DIRECTIVE_CLASS_SUBCLASS_NAME) {
      r = directive_class_subclass_name(b, 0);
    }
    else if (t == DIRECTIVE_CONSTANT) {
      r = directive_constant(b, 0);
    }
    else if (t == DIRECTIVE_CONSTANT_VALUE) {
      r = directive_constant_value(b, 0);
    }
    else if (t == DIRECTIVE_METHOD) {
      r = directive_method(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_ABSTRACT) {
      r = directive_method_abstract(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_ABSTRACT_DELEGATE_EXTERNAL) {
      r = directive_method_abstract_delegate_external(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_ACCESS) {
      r = directive_method_access(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_ATTRIBUTE) {
      r = directive_method_attribute(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_BODY) {
      r = directive_method_body(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_CLASS) {
      r = directive_method_class(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_DELEGATE) {
      r = directive_method_delegate(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_EXTERNAL) {
      r = directive_method_external(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_GUARDED_UNGUARDED) {
      r = directive_method_guarded_unguarded(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_NAME) {
      r = directive_method_name(b, 0);
    }
    else if (t == DIRECTIVE_METHOD_UNPROTECTED_PROTECTED) {
      r = directive_method_unprotected_protected(b, 0);
    }
    else if (t == DIRECTIVE_OPTIONS) {
      r = directive_options(b, 0);
    }
    else if (t == DIRECTIVE_OPTIONS_KEY_VALUE_PAIR) {
      r = directive_options_key_value_pair(b, 0);
    }
    else if (t == DIRECTIVE_REQUIRES) {
      r = directive_requires(b, 0);
    }
    else if (t == DIRECTIVE_REQUIRES_PATH) {
      r = directive_requires_path(b, 0);
    }
    else if (t == DIRECTIVE_RESOURCE) {
      r = directive_resource(b, 0);
    }
    else if (t == DIRECTIVE_RESOURCE_DATA) {
      r = directive_resource_data(b, 0);
    }
    else if (t == DIRECTIVE_RESOURCE_END) {
      r = directive_resource_end(b, 0);
    }
    else if (t == DIRECTIVE_ROUTINE) {
      r = directive_routine(b, 0);
    }
    else if (t == DIRECTIVE_ROUTINE_BODY) {
      r = directive_routine_body(b, 0);
    }
    else if (t == DIRECTIVE_ROUTINE_EXTERNAL) {
      r = directive_routine_external(b, 0);
    }
    else if (t == DIRECTIVE_ROUTINE_PRIVATE_PUBLIC) {
      r = directive_routine_private_public(b, 0);
    }
    else if (t == DIRECTIVE_START) {
      r = directive_start(b, 0);
    }
    else if (t == ENVIRONMENT_ID) {
      r = environment_id(b, 0);
    }
    else if (t == EXPR) {
      r = expr(b, 0);
    }
    else if (t == EXTENDED_ASSIGNMENT) {
      r = extended_assignment(b, 0);
    }
    else if (t == FUNCTION_CALL) {
      r = function_call(b, 0);
    }
    else if (t == ID) {
      r = id(b, 0);
    }
    else if (t == ID_WITH_LIT) {
      r = id_with_lit(b, 0);
    }
    else if (t == ID_WITH_NUMBER) {
      r = id_with_number(b, 0);
    }
    else if (t == INSTRUCTION) {
      r = instruction(b, 0);
    }
    else if (t == INSTRUCTION_TERMINATOR) {
      r = instruction_terminator(b, 0);
    }
    else if (t == KEYWORD_ADDRESS) {
      r = keyword_address(b, 0);
    }
    else if (t == KEYWORD_ARG) {
      r = keyword_arg(b, 0);
    }
    else if (t == KEYWORD_CALL) {
      r = keyword_call(b, 0);
    }
    else if (t == KEYWORD_CALL_OFF) {
      r = keyword_call_off(b, 0);
    }
    else if (t == KEYWORD_CALL_ON) {
      r = keyword_call_on(b, 0);
    }
    else if (t == KEYWORD_CALL_VARIANTS) {
      r = keyword_call_variants(b, 0);
    }
    else if (t == KEYWORD_DO) {
      r = keyword_do(b, 0);
    }
    else if (t == KEYWORD_DO_CONDITIONAL) {
      r = keyword_do_conditional(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR) {
      r = keyword_do_repetitor(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR_ASSIGN) {
      r = keyword_do_repetitor_assign(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR_BY) {
      r = keyword_do_repetitor_by(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR_FOR) {
      r = keyword_do_repetitor_for(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR_INDEX) {
      r = keyword_do_repetitor_index(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR_ITEM) {
      r = keyword_do_repetitor_item(b, 0);
    }
    else if (t == KEYWORD_DO_REPETITOR_TO) {
      r = keyword_do_repetitor_to(b, 0);
    }
    else if (t == KEYWORD_DROP) {
      r = keyword_drop(b, 0);
    }
    else if (t == KEYWORD_EXIT) {
      r = keyword_exit(b, 0);
    }
    else if (t == KEYWORD_EXPOSE) {
      r = keyword_expose(b, 0);
    }
    else if (t == KEYWORD_FORWARD) {
      r = keyword_forward(b, 0);
    }
    else if (t == KEYWORD_FORWARD_ARGUMENTS) {
      r = keyword_forward_arguments(b, 0);
    }
    else if (t == KEYWORD_FORWARD_ARRAY) {
      r = keyword_forward_array(b, 0);
    }
    else if (t == KEYWORD_FORWARD_CLASS) {
      r = keyword_forward_class(b, 0);
    }
    else if (t == KEYWORD_FORWARD_CONTINUE) {
      r = keyword_forward_continue(b, 0);
    }
    else if (t == KEYWORD_FORWARD_MESSAGE) {
      r = keyword_forward_message(b, 0);
    }
    else if (t == KEYWORD_FORWARD_TO) {
      r = keyword_forward_to(b, 0);
    }
    else if (t == KEYWORD_GUARD) {
      r = keyword_guard(b, 0);
    }
    else if (t == KEYWORD_IF) {
      r = keyword_if(b, 0);
    }
    else if (t == KEYWORD_IF_ELSE) {
      r = keyword_if_else(b, 0);
    }
    else if (t == KEYWORD_INSTRUCTION) {
      r = keyword_instruction(b, 0);
    }
    else if (t == KEYWORD_INTERPRET) {
      r = keyword_interpret(b, 0);
    }
    else if (t == KEYWORD_ITERATE) {
      r = keyword_iterate(b, 0);
    }
    else if (t == KEYWORD_LEAVE) {
      r = keyword_leave(b, 0);
    }
    else if (t == KEYWORD_LIST) {
      r = keyword_list(b, 0);
    }
    else if (t == KEYWORD_NOP) {
      r = keyword_nop(b, 0);
    }
    else if (t == KEYWORD_NUMERIC) {
      r = keyword_numeric(b, 0);
    }
    else if (t == KEYWORD_NUMERIC_FORM) {
      r = keyword_numeric_form(b, 0);
    }
    else if (t == KEYWORD_OPTIONS) {
      r = keyword_options(b, 0);
    }
    else if (t == KEYWORD_PARSE) {
      r = keyword_parse(b, 0);
    }
    else if (t == KEYWORD_PARSE_CASELESS) {
      r = keyword_parse_caseless(b, 0);
    }
    else if (t == KEYWORD_PARSE_UPPER_LOWER) {
      r = keyword_parse_upper_lower(b, 0);
    }
    else if (t == KEYWORD_PROCEDURE) {
      r = keyword_procedure(b, 0);
    }
    else if (t == KEYWORD_PULL) {
      r = keyword_pull(b, 0);
    }
    else if (t == KEYWORD_PUSH) {
      r = keyword_push(b, 0);
    }
    else if (t == KEYWORD_QUEUE) {
      r = keyword_queue(b, 0);
    }
    else if (t == KEYWORD_RAISE) {
      r = keyword_raise(b, 0);
    }
    else if (t == KEYWORD_RAISE_ERROR_ETC) {
      r = keyword_raise_error_etc(b, 0);
    }
    else if (t == KEYWORD_RAISE_ERROR_ETC_KEYWORDS) {
      r = keyword_raise_error_etc_keywords(b, 0);
    }
    else if (t == KEYWORD_RAISE_OPTIONS) {
      r = keyword_raise_options(b, 0);
    }
    else if (t == KEYWORD_RAISE_OPTIONS_ADDITIONAL) {
      r = keyword_raise_options_additional(b, 0);
    }
    else if (t == KEYWORD_RAISE_OPTIONS_ARRAY) {
      r = keyword_raise_options_array(b, 0);
    }
    else if (t == KEYWORD_RAISE_OPTIONS_DESCRIPTION) {
      r = keyword_raise_options_description(b, 0);
    }
    else if (t == KEYWORD_RAISE_OPTIONS_EXIT_RETURN) {
      r = keyword_raise_options_exit_return(b, 0);
    }
    else if (t == KEYWORD_REPLY) {
      r = keyword_reply(b, 0);
    }
    else if (t == KEYWORD_RETURN) {
      r = keyword_return(b, 0);
    }
    else if (t == KEYWORD_SAY) {
      r = keyword_say(b, 0);
    }
    else if (t == KEYWORD_SELECT) {
      r = keyword_select(b, 0);
    }
    else if (t == KEYWORD_SELECT_CASE) {
      r = keyword_select_case(b, 0);
    }
    else if (t == KEYWORD_SELECT_LABEL) {
      r = keyword_select_label(b, 0);
    }
    else if (t == KEYWORD_SELECT_OTHERWISE) {
      r = keyword_select_otherwise(b, 0);
    }
    else if (t == KEYWORD_SELECT_WHEN) {
      r = keyword_select_when(b, 0);
    }
    else if (t == KEYWORD_SIGNAL) {
      r = keyword_signal(b, 0);
    }
    else if (t == KEYWORD_SIGNAL_ANY_ETC) {
      r = keyword_signal_any_etc(b, 0);
    }
    else if (t == KEYWORD_SIGNAL_OFF) {
      r = keyword_signal_off(b, 0);
    }
    else if (t == KEYWORD_SIGNAL_ON) {
      r = keyword_signal_on(b, 0);
    }
    else if (t == KEYWORD_SIGNAL_ON_NAME) {
      r = keyword_signal_on_name(b, 0);
    }
    else if (t == KEYWORD_SIGNAL_VAL) {
      r = keyword_signal_val(b, 0);
    }
    else if (t == KEYWORD_TRACE) {
      r = keyword_trace(b, 0);
    }
    else if (t == KEYWORD_TRACE_KEYWORD_PART) {
      r = keyword_trace_keyword_part(b, 0);
    }
    else if (t == KEYWORD_USE) {
      r = keyword_use(b, 0);
    }
    else if (t == KEYWORD_USE_LOCAL) {
      r = keyword_use_local(b, 0);
    }
    else if (t == KEYWORD_USE_VARIANTS) {
      r = keyword_use_variants(b, 0);
    }
    else if (t == LABEL_INSTRUCTION) {
      r = label_instruction(b, 0);
    }
    else if (t == LITERAL_EXPR) {
      r = literal_expr(b, 0);
    }
    else if (t == MESSAGE_TERM) {
      r = message_term(b, 0);
    }
    else if (t == MESSAGE_TERM_BRACK) {
      r = message_term_brack(b, 0);
    }
    else if (t == MESSAGE_TERM_OPERATOR) {
      r = message_term_operator(b, 0);
    }
    else if (t == MESSAGE_TERM_RECEIVER) {
      r = message_term_receiver(b, 0);
    }
    else if (t == MESSAGE_TERM_TAIL) {
      r = message_term_tail(b, 0);
    }
    else if (t == MESSAGE_TERM_TILDE) {
      r = message_term_tilde(b, 0);
    }
    else if (t == NAMESPACE_PREFIX) {
      r = namespace_prefix(b, 0);
    }
    else if (t == NEGATIVE_NUMERIC_EXPR) {
      r = negative_numeric_expr(b, 0);
    }
    else if (t == NEW_LINE) {
      r = new_line(b, 0);
    }
    else if (t == NUMBER_LIT) {
      r = number_lit(b, 0);
    }
    else if (t == NUMERIC_EXPR) {
      r = numeric_expr(b, 0);
    }
    else if (t == PARAMETER_EXPR) {
      r = parameter_expr(b, 0);
    }
    else if (t == SHEBANG) {
      r = shebang(b, 0);
    }
    else if (t == STANDALONE_KEYWORD_MESSAGE_TERM) {
      r = standalone_keyword_message_term(b, 0);
    }
    else if (t == STRING_LIT) {
      r = string_lit(b, 0);
    }
    else if (t == SYMBOL_OR_STRING) {
      r = symbol_or_string(b, 0);
    }
    else if (t == SYMBOL_OR_STRING_OR_FUNCTION) {
      r = symbol_or_string_or_function(b, 0);
    }
    else if (t == SYMBOL_OR_STRING_OR_KEYWORD) {
      r = symbol_or_string_or_keyword(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return package_$(b, l + 1);
  }

  /* ********************************************************** */
  // (expr)? COMMA (COMMA | expr | <<paren array_term>> )*
  public static boolean array_term(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_term")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_TERM, "<array term>");
    r = array_term_0(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && array_term_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (expr)?
  private static boolean array_term_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_term_0")) return false;
    array_term_0_0(b, l + 1);
    return true;
  }

  // (expr)
  private static boolean array_term_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_term_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA | expr | <<paren array_term>> )*
  private static boolean array_term_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_term_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!array_term_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "array_term_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA | expr | <<paren array_term>>
  private static boolean array_term_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_term_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = expr(b, l + 1);
    if (!r) r = paren(b, l + 1, array_term_parser_);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LPAREN array_term RPAREN
  public static boolean array_term_parenthized(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_term_parenthized")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && array_term(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ARRAY_TERM_PARENTHIZED, r);
    return r;
  }

  /* ********************************************************** */
  // (message_term | id) extended_assignment expr
  public static boolean assignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignment")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT, "<assignment>");
    r = assignment_0(b, l + 1);
    r = r && extended_assignment(b, l + 1);
    p = r; // pin = 2
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // message_term | id
  private static boolean assignment_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignment_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term(b, l + 1);
    if (!r) r = id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // assignment |  <<excluding_expr keyword_list>> | (keyword_instruction)
  static boolean assignments_and_keywords(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignments_and_keywords")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignment(b, l + 1);
    if (!r) r = excluding_expr(b, l + 1, keyword_list_parser_);
    if (!r) r = assignments_and_keywords_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_instruction)
  private static boolean assignments_and_keywords_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignments_and_keywords_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_instruction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // comparator <<meta_expr <<p>>>>
  public static boolean bool_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "bool_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, BOOL_EXPR, null);
    r = comparator(b, l + 1);
    r = r && meta_expr(b, l + 1, _p);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FALSE | TRUE
  public static boolean bool_lit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bool_lit")) return false;
    if (!nextTokenIs(b, "<bool lit>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOL_LIT, "<bool lit>");
    r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, TRUE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BIF_ABBREV LPAREN expr COMMA parameter_expr (COMMA parameter_expr) RPAREN
  public static boolean built_in_abbrev(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_abbrev")) return false;
    if (!nextTokenIs(b, BIF_ABBREV)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_ABBREV, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_abbrev_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_ABBREV, r);
    return r;
  }

  // COMMA parameter_expr
  private static boolean built_in_abbrev_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_abbrev_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_ABS LPAREN parameter_expr RPAREN
  public static boolean built_in_abs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_abs")) return false;
    if (!nextTokenIs(b, BIF_ABS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_ABS, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_ABS, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_ADDRESS LPAREN RPAREN
  public static boolean built_in_address(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_address")) return false;
    if (!nextTokenIs(b, BIF_ADDRESS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_ADDRESS, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_ADDRESS, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_ARG LPAREN (parameter_expr (COMMA parameter_expr)?)? RPAREN
  public static boolean built_in_arg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_arg")) return false;
    if (!nextTokenIs(b, BIF_ARG)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_ARG, LPAREN);
    r = r && built_in_arg_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_ARG, r);
    return r;
  }

  // (parameter_expr (COMMA parameter_expr)?)?
  private static boolean built_in_arg_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_arg_2")) return false;
    built_in_arg_2_0(b, l + 1);
    return true;
  }

  // parameter_expr (COMMA parameter_expr)?
  private static boolean built_in_arg_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_arg_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_expr(b, l + 1);
    r = r && built_in_arg_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_arg_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_arg_2_0_1")) return false;
    built_in_arg_2_0_1_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_arg_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_arg_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_B2X LPAREN parameter_expr RPAREN
  public static boolean built_in_b2x(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_b2x")) return false;
    if (!nextTokenIs(b, BIF_B2X)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_B2X, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_B_2_X, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_BEEP parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_beep(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_beep")) return false;
    if (!nextTokenIs(b, BIF_BEEP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BIF_BEEP);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_BEEP, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_BITAND LPAREN parameter_expr COMMA parameter_expr? (COMMA parameter_expr) RPAREN
  public static boolean built_in_bitand(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitand")) return false;
    if (!nextTokenIs(b, BIF_BITAND)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_BITAND, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_bitand_4(b, l + 1);
    r = r && built_in_bitand_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_BITAND, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_bitand_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitand_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_bitand_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitand_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_BITOR LPAREN parameter_expr COMMA parameter_expr? (COMMA parameter_expr) RPAREN
  public static boolean built_in_bitor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitor")) return false;
    if (!nextTokenIs(b, BIF_BITOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_BITOR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_bitor_4(b, l + 1);
    r = r && built_in_bitor_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_BITOR, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_bitor_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitor_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_bitor_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitor_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_BITXOR LPAREN parameter_expr COMMA parameter_expr? (COMMA parameter_expr) RPAREN
  public static boolean built_in_bitxor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitxor")) return false;
    if (!nextTokenIs(b, BIF_BITXOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_BITXOR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_bitxor_4(b, l + 1);
    r = r && built_in_bitxor_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_BITXOR, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_bitxor_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitxor_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_bitxor_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_bitxor_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_C2D LPAREN parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_c2d(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_c2d")) return false;
    if (!nextTokenIs(b, BIF_C2D)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_C2D, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_c2d_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_C_2_D, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_c2d_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_c2d_3")) return false;
    built_in_c2d_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_c2d_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_c2d_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_C2D LPAREN parameter_expr RPAREN
  public static boolean built_in_c2x(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_c2x")) return false;
    if (!nextTokenIs(b, BIF_C2D)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_C2D, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_C_2_X, r);
    return r;
  }

  /* ********************************************************** */
  // (BIF_CENTER | BIF_CENTRE) LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_center(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_center")) return false;
    if (!nextTokenIs(b, "<built in center>", BIF_CENTER, BIF_CENTRE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BUILT_IN_CENTER, "<built in center>");
    r = built_in_center_0(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BIF_CENTER | BIF_CENTRE
  private static boolean built_in_center_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_center_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BIF_CENTER);
    if (!r) r = consumeToken(b, BIF_CENTRE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_CHANGESTR LPAREN parameter_expr COMMA parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_changestr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_changestr")) return false;
    if (!nextTokenIs(b, BIF_CHANGESTR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_CHANGESTR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_changestr_7(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_CHANGESTR, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_changestr_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_changestr_7")) return false;
    built_in_changestr_7_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_changestr_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_changestr_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_CHARIN LPAREN parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_charin(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charin")) return false;
    if (!nextTokenIs(b, BIF_CHARIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_CHARIN, LPAREN);
    r = r && built_in_charin_2(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_charin_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_charin_6(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_CHARIN, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_charin_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charin_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_charin_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charin_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_charin_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charin_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_CHAROUT LPAREN parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_charout(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charout")) return false;
    if (!nextTokenIs(b, BIF_CHAROUT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_CHAROUT, LPAREN);
    r = r && built_in_charout_2(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_charout_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_charout_6(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_CHAROUT, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_charout_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charout_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_charout_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charout_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_charout_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_charout_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_CHARS LPAREN parameter_expr? RPAREN
  public static boolean built_in_chars(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_chars")) return false;
    if (!nextTokenIs(b, BIF_CHARS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_CHARS, LPAREN);
    r = r && built_in_chars_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_CHARS, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_chars_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_chars_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_COMPARE LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_compare(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_compare")) return false;
    if (!nextTokenIs(b, BIF_COMPARE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_COMPARE, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_compare_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_COMPARE, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_compare_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_compare_5")) return false;
    built_in_compare_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_compare_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_compare_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_CONDITION LPAREN parameter_expr? RPAREN
  public static boolean built_in_condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_condition")) return false;
    if (!nextTokenIs(b, BIF_CONDITION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_CONDITION, LPAREN);
    r = r && built_in_condition_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_CONDITION, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_condition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_condition_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_COPIES LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_copies(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_copies")) return false;
    if (!nextTokenIs(b, BIF_COPIES)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_COPIES, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_COPIES, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_COUNTSTR LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_countstr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_countstr")) return false;
    if (!nextTokenIs(b, BIF_COUNTSTR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_COUNTSTR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_COUNTSTR, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_D2C LPAREN parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_d2c(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_d2c")) return false;
    if (!nextTokenIs(b, BIF_D2C)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_D2C, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_d2c_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_D_2_C, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_d2c_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_d2c_3")) return false;
    built_in_d2c_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_d2c_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_d2c_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_D2X LPAREN parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_d2x(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_d2x")) return false;
    if (!nextTokenIs(b, BIF_D2X)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_D2X, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_d2x_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_D_2_X, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_d2x_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_d2x_3")) return false;
    built_in_d2x_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_d2x_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_d2x_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_DATATYPE parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_datatype(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_datatype")) return false;
    if (!nextTokenIs(b, BIF_DATATYPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BIF_DATATYPE);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_datatype_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_DATATYPE, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_datatype_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_datatype_2")) return false;
    built_in_datatype_2_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_datatype_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_datatype_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_DATE LPAREN parameter_expr? COMMA (COMMA | (parameter_expr COMMA parameter_expr?)) COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_date(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date")) return false;
    if (!nextTokenIs(b, BIF_DATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_DATE, LPAREN);
    r = r && built_in_date_2(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_date_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_date_6(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_date_8(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_DATE, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_date_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // COMMA | (parameter_expr COMMA parameter_expr?)
  private static boolean built_in_date_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = built_in_date_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr COMMA parameter_expr?
  private static boolean built_in_date_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date_4_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_date_4_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_date_4_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date_4_1_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_date_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_date_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_date_8")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_DELSTR LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_delstr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_delstr")) return false;
    if (!nextTokenIs(b, BIF_DELSTR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_DELSTR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_delstr_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_DELSTR, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_delstr_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_delstr_5")) return false;
    built_in_delstr_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_delstr_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_delstr_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_DELWORD LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_delword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_delword")) return false;
    if (!nextTokenIs(b, BIF_DELWORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_DELWORD, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_delword_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_DELWORD, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_delword_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_delword_5")) return false;
    built_in_delword_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_delword_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_delword_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_DIGITS LPAREN RPAREN
  public static boolean built_in_digits(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_digits")) return false;
    if (!nextTokenIs(b, BIF_DIGITS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_DIGITS, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_DIGITS, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_DIRECTORY LPAREN parameter_expr RPAREN
  public static boolean built_in_directory(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_directory")) return false;
    if (!nextTokenIs(b, BIF_DIRECTORY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_DIRECTORY, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_DIRECTORY, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_ENDLOCAL LPAREN RPAREN
  public static boolean built_in_endlocal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_endlocal")) return false;
    if (!nextTokenIs(b, BIF_ENDLOCAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_ENDLOCAL, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_ENDLOCAL, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_ERRORTEXT LPAREN parameter_expr RPAREN
  public static boolean built_in_errortext(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_errortext")) return false;
    if (!nextTokenIs(b, BIF_ERRORTEXT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_ERRORTEXT, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_ERRORTEXT, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_FILESPEC LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_filespec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_filespec")) return false;
    if (!nextTokenIs(b, BIF_FILESPEC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_FILESPEC, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_FILESPEC, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_FORM LPAREN RPAREN
  public static boolean built_in_form(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_form")) return false;
    if (!nextTokenIs(b, BIF_FORM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_FORM, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_FORM, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_FORMAT LPAREN parameter_expr COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_format(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_format")) return false;
    if (!nextTokenIs(b, BIF_FORMAT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_FORMAT, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_format_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_format_6(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_format_8(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_format_10(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_FORMAT, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_format_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_format_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_format_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_format_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_format_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_format_8")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_format_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_format_10")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // built_in_abbrev | built_in_abs | built_in_address | built_in_arg | built_in_b2x | built_in_beep | built_in_bitand | built_in_bitor | built_in_bitxor | built_in_c2d | built_in_c2x | built_in_center  | built_in_changestr | built_in_charin |
  //                                built_in_charout | built_in_chars | built_in_compare | built_in_condition | built_in_copies | built_in_countstr | built_in_d2c | built_in_d2x | built_in_datatype | built_in_date | built_in_delstr | built_in_delword | built_in_digits
  //                                | built_in_directory | built_in_endlocal | built_in_errortext | built_in_filespec | built_in_form | built_in_format | built_in_fuzz | built_in_insert | built_in_lastpos | built_in_left | built_in_length | built_in_linein | built_in_lineout | built_in_lines | built_in_lower | built_in_max | built_in_min | built_in_overlay
  //                                | built_in_pos | built_in_qualify | built_in_queued | built_in_random | built_in_reverse | built_in_right | built_in_rxfuncadd | built_in_rxfuncdrop |  built_in_rxfuncquery | built_in_rxqueue | built_in_setlocal | built_in_sign | built_in_sourceline | built_in_space | built_in_stream | built_in_strip | built_in_substr | built_in_subword | built_in_symbol
  //                                | built_in_time | built_in_trace | built_in_translate | built_in_trunc | built_in_upper | built_in_userid | built_in_value | built_in_var | built_in_verify | built_in_word | built_in_wordindex | built_in_wordlength | built_in_wordpos | built_in_words | built_in_x2b | built_in_x2c | built_in_x2d | built_in_xrange
  static boolean built_in_functions(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_functions")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = built_in_abbrev(b, l + 1);
    if (!r) r = built_in_abs(b, l + 1);
    if (!r) r = built_in_address(b, l + 1);
    if (!r) r = built_in_arg(b, l + 1);
    if (!r) r = built_in_b2x(b, l + 1);
    if (!r) r = built_in_beep(b, l + 1);
    if (!r) r = built_in_bitand(b, l + 1);
    if (!r) r = built_in_bitor(b, l + 1);
    if (!r) r = built_in_bitxor(b, l + 1);
    if (!r) r = built_in_c2d(b, l + 1);
    if (!r) r = built_in_c2x(b, l + 1);
    if (!r) r = built_in_center(b, l + 1);
    if (!r) r = built_in_changestr(b, l + 1);
    if (!r) r = built_in_charin(b, l + 1);
    if (!r) r = built_in_charout(b, l + 1);
    if (!r) r = built_in_chars(b, l + 1);
    if (!r) r = built_in_compare(b, l + 1);
    if (!r) r = built_in_condition(b, l + 1);
    if (!r) r = built_in_copies(b, l + 1);
    if (!r) r = built_in_countstr(b, l + 1);
    if (!r) r = built_in_d2c(b, l + 1);
    if (!r) r = built_in_d2x(b, l + 1);
    if (!r) r = built_in_datatype(b, l + 1);
    if (!r) r = built_in_date(b, l + 1);
    if (!r) r = built_in_delstr(b, l + 1);
    if (!r) r = built_in_delword(b, l + 1);
    if (!r) r = built_in_digits(b, l + 1);
    if (!r) r = built_in_directory(b, l + 1);
    if (!r) r = built_in_endlocal(b, l + 1);
    if (!r) r = built_in_errortext(b, l + 1);
    if (!r) r = built_in_filespec(b, l + 1);
    if (!r) r = built_in_form(b, l + 1);
    if (!r) r = built_in_format(b, l + 1);
    if (!r) r = built_in_fuzz(b, l + 1);
    if (!r) r = built_in_insert(b, l + 1);
    if (!r) r = built_in_lastpos(b, l + 1);
    if (!r) r = built_in_left(b, l + 1);
    if (!r) r = built_in_length(b, l + 1);
    if (!r) r = built_in_linein(b, l + 1);
    if (!r) r = built_in_lineout(b, l + 1);
    if (!r) r = built_in_lines(b, l + 1);
    if (!r) r = built_in_lower(b, l + 1);
    if (!r) r = built_in_max(b, l + 1);
    if (!r) r = built_in_min(b, l + 1);
    if (!r) r = built_in_overlay(b, l + 1);
    if (!r) r = built_in_pos(b, l + 1);
    if (!r) r = built_in_qualify(b, l + 1);
    if (!r) r = built_in_queued(b, l + 1);
    if (!r) r = built_in_random(b, l + 1);
    if (!r) r = built_in_reverse(b, l + 1);
    if (!r) r = built_in_right(b, l + 1);
    if (!r) r = built_in_rxfuncadd(b, l + 1);
    if (!r) r = built_in_rxfuncdrop(b, l + 1);
    if (!r) r = built_in_rxfuncquery(b, l + 1);
    if (!r) r = built_in_rxqueue(b, l + 1);
    if (!r) r = built_in_setlocal(b, l + 1);
    if (!r) r = built_in_sign(b, l + 1);
    if (!r) r = built_in_sourceline(b, l + 1);
    if (!r) r = built_in_space(b, l + 1);
    if (!r) r = built_in_stream(b, l + 1);
    if (!r) r = built_in_strip(b, l + 1);
    if (!r) r = built_in_substr(b, l + 1);
    if (!r) r = built_in_subword(b, l + 1);
    if (!r) r = built_in_symbol(b, l + 1);
    if (!r) r = built_in_time(b, l + 1);
    if (!r) r = built_in_trace(b, l + 1);
    if (!r) r = built_in_translate(b, l + 1);
    if (!r) r = built_in_trunc(b, l + 1);
    if (!r) r = built_in_upper(b, l + 1);
    if (!r) r = built_in_userid(b, l + 1);
    if (!r) r = built_in_value(b, l + 1);
    if (!r) r = built_in_var(b, l + 1);
    if (!r) r = built_in_verify(b, l + 1);
    if (!r) r = built_in_word(b, l + 1);
    if (!r) r = built_in_wordindex(b, l + 1);
    if (!r) r = built_in_wordlength(b, l + 1);
    if (!r) r = built_in_wordpos(b, l + 1);
    if (!r) r = built_in_words(b, l + 1);
    if (!r) r = built_in_x2b(b, l + 1);
    if (!r) r = built_in_x2c(b, l + 1);
    if (!r) r = built_in_x2d(b, l + 1);
    if (!r) r = built_in_xrange(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_ABBREV | BIF_ABS | BIF_ADDRESS | BIF_ARG | BIF_B2X | BIF_BEEP | BIF_BITAND | BIF_BITOR | BIF_BITXOR | BIF_C2D | BIF_C2X | BIF_CENTER | BIF_CENTRE | BIF_CHANGESTR | BIF_CHARIN |
  // BIF_CHAROUT | BIF_CHARS | BIF_COMPARE | BIF_CONDITION | BIF_COPIES | BIF_COUNTSTR | BIF_D2C | BIF_D2X | BIF_DATATYPE | BIF_DATE | BIF_DELSTR | BIF_DELWORD | BIF_DIGITS | BIF_DIRECTORY | BIF_ENDLOCAL | BIF_ERRORTEXT | BIF_FILESPEC | BIF_FORM |
  // BIF_FORMAT | BIF_FUZZ | BIF_INSERT | BIF_LASTPOS | BIF_LEFT | BIF_LENGTH | BIF_LINEIN | BIF_LINEOUT | BIF_LINES | BIF_LOWER | BIF_MAX | BIF_MIN | BIF_OVERLAY | BIF_POS | BIF_QUALIFY | BIF_QUEUED | BIF_RANDOM |
  // BIF_REVERSE | BIF_RIGHT | BIF_RXFUNCADD | BIF_RXFUNCDROP | BIF_RXFUNCQUERY | BIF_RXQUEUE | BIF_SETLOCAL | BIF_SIGN | BIF_SOURCELINE | BIF_SPACE | BIF_STREAM | BIF_STRIP | BIF_SUBSTR | BIF_SUBWORD | BIF_SYMBOL | BIF_TIME | BIF_TRACE | BIF_TRANSLATE |
  // BIF_TRUNC | BIF_UPPER | BIF_USERID | BIF_VALUE | BIF_VAR | BIF_VERIFY | BIF_WORD | BIF_WORDINDEX | BIF_WORDLENGTH | BIF_WORDPOS | BIF_WORDS | BIF_X2B | BIF_X2C | BIF_X2D | BIF_XRANGE
  public static boolean built_in_functions_keyword_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_functions_keyword_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BUILT_IN_FUNCTIONS_KEYWORD_LIST, "<built in functions keyword list>");
    r = consumeToken(b, BIF_ABBREV);
    if (!r) r = consumeToken(b, BIF_ABS);
    if (!r) r = consumeToken(b, BIF_ADDRESS);
    if (!r) r = consumeToken(b, BIF_ARG);
    if (!r) r = consumeToken(b, BIF_B2X);
    if (!r) r = consumeToken(b, BIF_BEEP);
    if (!r) r = consumeToken(b, BIF_BITAND);
    if (!r) r = consumeToken(b, BIF_BITOR);
    if (!r) r = consumeToken(b, BIF_BITXOR);
    if (!r) r = consumeToken(b, BIF_C2D);
    if (!r) r = consumeToken(b, BIF_C2X);
    if (!r) r = consumeToken(b, BIF_CENTER);
    if (!r) r = consumeToken(b, BIF_CENTRE);
    if (!r) r = consumeToken(b, BIF_CHANGESTR);
    if (!r) r = consumeToken(b, BIF_CHARIN);
    if (!r) r = consumeToken(b, BIF_CHAROUT);
    if (!r) r = consumeToken(b, BIF_CHARS);
    if (!r) r = consumeToken(b, BIF_COMPARE);
    if (!r) r = consumeToken(b, BIF_CONDITION);
    if (!r) r = consumeToken(b, BIF_COPIES);
    if (!r) r = consumeToken(b, BIF_COUNTSTR);
    if (!r) r = consumeToken(b, BIF_D2C);
    if (!r) r = consumeToken(b, BIF_D2X);
    if (!r) r = consumeToken(b, BIF_DATATYPE);
    if (!r) r = consumeToken(b, BIF_DATE);
    if (!r) r = consumeToken(b, BIF_DELSTR);
    if (!r) r = consumeToken(b, BIF_DELWORD);
    if (!r) r = consumeToken(b, BIF_DIGITS);
    if (!r) r = consumeToken(b, BIF_DIRECTORY);
    if (!r) r = consumeToken(b, BIF_ENDLOCAL);
    if (!r) r = consumeToken(b, BIF_ERRORTEXT);
    if (!r) r = consumeToken(b, BIF_FILESPEC);
    if (!r) r = consumeToken(b, BIF_FORM);
    if (!r) r = consumeToken(b, BIF_FORMAT);
    if (!r) r = consumeToken(b, BIF_FUZZ);
    if (!r) r = consumeToken(b, BIF_INSERT);
    if (!r) r = consumeToken(b, BIF_LASTPOS);
    if (!r) r = consumeToken(b, BIF_LEFT);
    if (!r) r = consumeToken(b, BIF_LENGTH);
    if (!r) r = consumeToken(b, BIF_LINEIN);
    if (!r) r = consumeToken(b, BIF_LINEOUT);
    if (!r) r = consumeToken(b, BIF_LINES);
    if (!r) r = consumeToken(b, BIF_LOWER);
    if (!r) r = consumeToken(b, BIF_MAX);
    if (!r) r = consumeToken(b, BIF_MIN);
    if (!r) r = consumeToken(b, BIF_OVERLAY);
    if (!r) r = consumeToken(b, BIF_POS);
    if (!r) r = consumeToken(b, BIF_QUALIFY);
    if (!r) r = consumeToken(b, BIF_QUEUED);
    if (!r) r = consumeToken(b, BIF_RANDOM);
    if (!r) r = consumeToken(b, BIF_REVERSE);
    if (!r) r = consumeToken(b, BIF_RIGHT);
    if (!r) r = consumeToken(b, BIF_RXFUNCADD);
    if (!r) r = consumeToken(b, BIF_RXFUNCDROP);
    if (!r) r = consumeToken(b, BIF_RXFUNCQUERY);
    if (!r) r = consumeToken(b, BIF_RXQUEUE);
    if (!r) r = consumeToken(b, BIF_SETLOCAL);
    if (!r) r = consumeToken(b, BIF_SIGN);
    if (!r) r = consumeToken(b, BIF_SOURCELINE);
    if (!r) r = consumeToken(b, BIF_SPACE);
    if (!r) r = consumeToken(b, BIF_STREAM);
    if (!r) r = consumeToken(b, BIF_STRIP);
    if (!r) r = consumeToken(b, BIF_SUBSTR);
    if (!r) r = consumeToken(b, BIF_SUBWORD);
    if (!r) r = consumeToken(b, BIF_SYMBOL);
    if (!r) r = consumeToken(b, BIF_TIME);
    if (!r) r = consumeToken(b, BIF_TRACE);
    if (!r) r = consumeToken(b, BIF_TRANSLATE);
    if (!r) r = consumeToken(b, BIF_TRUNC);
    if (!r) r = consumeToken(b, BIF_UPPER);
    if (!r) r = consumeToken(b, BIF_USERID);
    if (!r) r = consumeToken(b, BIF_VALUE);
    if (!r) r = consumeToken(b, BIF_VAR);
    if (!r) r = consumeToken(b, BIF_VERIFY);
    if (!r) r = consumeToken(b, BIF_WORD);
    if (!r) r = consumeToken(b, BIF_WORDINDEX);
    if (!r) r = consumeToken(b, BIF_WORDLENGTH);
    if (!r) r = consumeToken(b, BIF_WORDPOS);
    if (!r) r = consumeToken(b, BIF_WORDS);
    if (!r) r = consumeToken(b, BIF_X2B);
    if (!r) r = consumeToken(b, BIF_X2C);
    if (!r) r = consumeToken(b, BIF_X2D);
    if (!r) r = consumeToken(b, BIF_XRANGE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BIF_FUZZ LPAREN RPAREN
  public static boolean built_in_fuzz(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_fuzz")) return false;
    if (!nextTokenIs(b, BIF_FUZZ)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_FUZZ, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_FUZZ, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_INSERT LPAREN parameter_expr COMMA parameter_expr COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_insert(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_insert")) return false;
    if (!nextTokenIs(b, BIF_INSERT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_INSERT, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_insert_6(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_insert_8(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_insert_10(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_INSERT, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_insert_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_insert_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_insert_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_insert_8")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_insert_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_insert_10")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_LASTPOS LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr? (COMMA parameter_expr)?)? RPAREN
  public static boolean built_in_lastpos(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lastpos")) return false;
    if (!nextTokenIs(b, BIF_LASTPOS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LASTPOS, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_lastpos_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LASTPOS, r);
    return r;
  }

  // (COMMA parameter_expr? (COMMA parameter_expr)?)?
  private static boolean built_in_lastpos_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lastpos_5")) return false;
    built_in_lastpos_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr? (COMMA parameter_expr)?
  private static boolean built_in_lastpos_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lastpos_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && built_in_lastpos_5_0_1(b, l + 1);
    r = r && built_in_lastpos_5_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_lastpos_5_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lastpos_5_0_1")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_lastpos_5_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lastpos_5_0_2")) return false;
    built_in_lastpos_5_0_2_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_lastpos_5_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lastpos_5_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_LEFT LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_left(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_left")) return false;
    if (!nextTokenIs(b, BIF_LEFT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LEFT, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_left_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LEFT, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_left_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_left_5")) return false;
    built_in_left_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_left_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_left_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_LENGTH LPAREN parameter_expr RPAREN
  public static boolean built_in_length(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_length")) return false;
    if (!nextTokenIs(b, BIF_LENGTH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LENGTH, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LENGTH, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_LINEIN LPAREN parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_linein(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_linein")) return false;
    if (!nextTokenIs(b, BIF_LINEIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LINEIN, LPAREN);
    r = r && built_in_linein_2(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_linein_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_linein_6(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LINEIN, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_linein_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_linein_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_linein_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_linein_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_linein_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_linein_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_LINEOUT LPAREN parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_lineout(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lineout")) return false;
    if (!nextTokenIs(b, BIF_LINEOUT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LINEOUT, LPAREN);
    r = r && built_in_lineout_2(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_lineout_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_lineout_6(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LINEOUT, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_lineout_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lineout_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_lineout_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lineout_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_lineout_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lineout_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_LINES LPAREN parameter_expr? ((COMMA NORMAL) | (COMMA COUNT))? RPAREN
  public static boolean built_in_lines(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lines")) return false;
    if (!nextTokenIs(b, BIF_LINES)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LINES, LPAREN);
    r = r && built_in_lines_2(b, l + 1);
    r = r && built_in_lines_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LINES, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_lines_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lines_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // ((COMMA NORMAL) | (COMMA COUNT))?
  private static boolean built_in_lines_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lines_3")) return false;
    built_in_lines_3_0(b, l + 1);
    return true;
  }

  // (COMMA NORMAL) | (COMMA COUNT)
  private static boolean built_in_lines_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lines_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = built_in_lines_3_0_0(b, l + 1);
    if (!r) r = built_in_lines_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA NORMAL
  private static boolean built_in_lines_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lines_3_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, NORMAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA COUNT
  private static boolean built_in_lines_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lines_3_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, COUNT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_LOWER LPAREN parameter_expr (COMMA parameter_expr? (COMMA parameter_expr)?)? RPAREN
  public static boolean built_in_lower(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lower")) return false;
    if (!nextTokenIs(b, BIF_LOWER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_LOWER, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_lower_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_LOWER, r);
    return r;
  }

  // (COMMA parameter_expr? (COMMA parameter_expr)?)?
  private static boolean built_in_lower_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lower_3")) return false;
    built_in_lower_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr? (COMMA parameter_expr)?
  private static boolean built_in_lower_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lower_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && built_in_lower_3_0_1(b, l + 1);
    r = r && built_in_lower_3_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_lower_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lower_3_0_1")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_lower_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lower_3_0_2")) return false;
    built_in_lower_3_0_2_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_lower_3_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_lower_3_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_MAX LPAREN parameter_expr (COMMA parameter_expr)* RPAREN
  public static boolean built_in_max(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_max")) return false;
    if (!nextTokenIs(b, BIF_MAX)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_MAX, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_max_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_MAX, r);
    return r;
  }

  // (COMMA parameter_expr)*
  private static boolean built_in_max_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_max_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!built_in_max_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "built_in_max_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_max_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_max_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_MIN LPAREN parameter_expr (COMMA parameter_expr)* RPAREN
  public static boolean built_in_min(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_min")) return false;
    if (!nextTokenIs(b, BIF_MIN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_MIN, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_min_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_MIN, r);
    return r;
  }

  // (COMMA parameter_expr)*
  private static boolean built_in_min_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_min_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!built_in_min_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "built_in_min_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_min_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_min_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_OVERLAY LPAREN parameter_expr COMMA parameter_expr COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr RPAREN
  public static boolean built_in_overlay(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_overlay")) return false;
    if (!nextTokenIs(b, BIF_OVERLAY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_OVERLAY, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_overlay_6(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_overlay_8(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_OVERLAY, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_overlay_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_overlay_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_overlay_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_overlay_8")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_POS LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr? (COMMA parameter_expr)?)? RPAREN
  public static boolean built_in_pos(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_pos")) return false;
    if (!nextTokenIs(b, BIF_POS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_POS, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_pos_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_POS, r);
    return r;
  }

  // (COMMA parameter_expr? (COMMA parameter_expr)?)?
  private static boolean built_in_pos_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_pos_5")) return false;
    built_in_pos_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr? (COMMA parameter_expr)?
  private static boolean built_in_pos_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_pos_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && built_in_pos_5_0_1(b, l + 1);
    r = r && built_in_pos_5_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_pos_5_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_pos_5_0_1")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_pos_5_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_pos_5_0_2")) return false;
    built_in_pos_5_0_2_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_pos_5_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_pos_5_0_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_QUALIFY LPAREN parameter_expr RPAREN
  public static boolean built_in_qualify(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_qualify")) return false;
    if (!nextTokenIs(b, BIF_QUALIFY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_QUALIFY, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_QUALIFY, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_QUEUED LPAREN RPAREN
  public static boolean built_in_queued(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_queued")) return false;
    if (!nextTokenIs(b, BIF_QUEUED)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_QUEUED, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_QUEUED, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_RANDOM LPAREN (parameter_expr | (parameter_expr? COMMA parameter_expr? COMMA parameter_expr?)) RPAREN
  public static boolean built_in_random(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_random")) return false;
    if (!nextTokenIs(b, BIF_RANDOM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_RANDOM, LPAREN);
    r = r && built_in_random_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_RANDOM, r);
    return r;
  }

  // parameter_expr | (parameter_expr? COMMA parameter_expr? COMMA parameter_expr?)
  private static boolean built_in_random_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_random_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_expr(b, l + 1);
    if (!r) r = built_in_random_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr? COMMA parameter_expr? COMMA parameter_expr?
  private static boolean built_in_random_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_random_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = built_in_random_2_1_0(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_random_2_1_2(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_random_2_1_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_random_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_random_2_1_0")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_random_2_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_random_2_1_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_random_2_1_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_random_2_1_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_REVERSE LPAREN parameter_expr RPAREN
  public static boolean built_in_reverse(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_reverse")) return false;
    if (!nextTokenIs(b, BIF_REVERSE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_REVERSE, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_REVERSE, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_RIGHT LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_right(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_right")) return false;
    if (!nextTokenIs(b, BIF_RIGHT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_RIGHT, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_right_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_RIGHT, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_right_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_right_5")) return false;
    built_in_right_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_right_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_right_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_RXFUNCADD LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_rxfuncadd(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxfuncadd")) return false;
    if (!nextTokenIs(b, BIF_RXFUNCADD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_RXFUNCADD, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_rxfuncadd_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_RXFUNCADD, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_rxfuncadd_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxfuncadd_5")) return false;
    built_in_rxfuncadd_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_rxfuncadd_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxfuncadd_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_RXFUNCDROP LPAREN parameter_expr RPAREN
  public static boolean built_in_rxfuncdrop(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxfuncdrop")) return false;
    if (!nextTokenIs(b, BIF_RXFUNCDROP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_RXFUNCDROP, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_RXFUNCDROP, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_RXFUNCQUERY LPAREN parameter_expr RPAREN
  public static boolean built_in_rxfuncquery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxfuncquery")) return false;
    if (!nextTokenIs(b, BIF_RXFUNCQUERY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_RXFUNCQUERY, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_RXFUNCQUERY, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_RXQUEUE LPAREN ((CREATE (COMMA parameter_expr)?) |
  //                 (DELETE COMMA parameter_expr) |
  //                 (EXIST COMMA parameter_expr) |
  //                 (GET) |
  //                 (OPEN COMMA parameter_expr) |
  //                 (SET COMMA parameter_expr)) RPAREN
  public static boolean built_in_rxqueue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue")) return false;
    if (!nextTokenIs(b, BIF_RXQUEUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_RXQUEUE, LPAREN);
    r = r && built_in_rxqueue_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_RXQUEUE, r);
    return r;
  }

  // (CREATE (COMMA parameter_expr)?) |
  //                 (DELETE COMMA parameter_expr) |
  //                 (EXIST COMMA parameter_expr) |
  //                 (GET) |
  //                 (OPEN COMMA parameter_expr) |
  //                 (SET COMMA parameter_expr)
  private static boolean built_in_rxqueue_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = built_in_rxqueue_2_0(b, l + 1);
    if (!r) r = built_in_rxqueue_2_1(b, l + 1);
    if (!r) r = built_in_rxqueue_2_2(b, l + 1);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = built_in_rxqueue_2_4(b, l + 1);
    if (!r) r = built_in_rxqueue_2_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CREATE (COMMA parameter_expr)?
  private static boolean built_in_rxqueue_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CREATE);
    r = r && built_in_rxqueue_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_rxqueue_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_0_1")) return false;
    built_in_rxqueue_2_0_1_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_rxqueue_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DELETE COMMA parameter_expr
  private static boolean built_in_rxqueue_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DELETE, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EXIST COMMA parameter_expr
  private static boolean built_in_rxqueue_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, EXIST, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // OPEN COMMA parameter_expr
  private static boolean built_in_rxqueue_2_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, OPEN, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SET COMMA parameter_expr
  private static boolean built_in_rxqueue_2_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_rxqueue_2_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, SET, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_SETLOCAL LPAREN RPAREN
  public static boolean built_in_setlocal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_setlocal")) return false;
    if (!nextTokenIs(b, BIF_SETLOCAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SETLOCAL, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_SETLOCAL, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_SIGN LPAREN parameter_expr RPAREN
  public static boolean built_in_sign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_sign")) return false;
    if (!nextTokenIs(b, BIF_SIGN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SIGN, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_SIGN, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_SOURCELINE LPAREN parameter_expr? RPAREN
  public static boolean built_in_sourceline(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_sourceline")) return false;
    if (!nextTokenIs(b, BIF_SOURCELINE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SOURCELINE, LPAREN);
    r = r && built_in_sourceline_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_SOURCELINE, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_sourceline_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_sourceline_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_SPACE LPAREN parameter_expr COMMA parameter_expr? (COMMA parameter_expr) RPAREN
  public static boolean built_in_space(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_space")) return false;
    if (!nextTokenIs(b, BIF_SPACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SPACE, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_space_4(b, l + 1);
    r = r && built_in_space_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_SPACE, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_space_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_space_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_space_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_space_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_STREAM LPAREN parameter_expr COMMA (STATE | (COMMAND COMMA parameter_expr) | DESCRIPTION )? RPAREN
  public static boolean built_in_stream(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_stream")) return false;
    if (!nextTokenIs(b, BIF_STREAM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_STREAM, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_stream_4(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_STREAM, r);
    return r;
  }

  // (STATE | (COMMAND COMMA parameter_expr) | DESCRIPTION )?
  private static boolean built_in_stream_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_stream_4")) return false;
    built_in_stream_4_0(b, l + 1);
    return true;
  }

  // STATE | (COMMAND COMMA parameter_expr) | DESCRIPTION
  private static boolean built_in_stream_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_stream_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STATE);
    if (!r) r = built_in_stream_4_0_1(b, l + 1);
    if (!r) r = consumeToken(b, DESCRIPTION);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMAND COMMA parameter_expr
  private static boolean built_in_stream_4_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_stream_4_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMAND, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_STRIP LPAREN parameter_expr COMMA ("B" | parameter_expr)? (COMMA parameter_expr)? RPAREN
  public static boolean built_in_strip(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_strip")) return false;
    if (!nextTokenIs(b, BIF_STRIP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_STRIP, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_strip_4(b, l + 1);
    r = r && built_in_strip_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_STRIP, r);
    return r;
  }

  // ("B" | parameter_expr)?
  private static boolean built_in_strip_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_strip_4")) return false;
    built_in_strip_4_0(b, l + 1);
    return true;
  }

  // "B" | parameter_expr
  private static boolean built_in_strip_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_strip_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "B");
    if (!r) r = parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_strip_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_strip_5")) return false;
    built_in_strip_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_strip_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_strip_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_SUBSTR LPAREN parameter_expr COMMA parameter_expr COMMA parameter_expr? (COMMA parameter_expr)? RPAREN
  public static boolean built_in_substr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_substr")) return false;
    if (!nextTokenIs(b, BIF_SUBSTR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SUBSTR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_substr_6(b, l + 1);
    r = r && built_in_substr_7(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_SUBSTR, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_substr_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_substr_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_substr_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_substr_7")) return false;
    built_in_substr_7_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_substr_7_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_substr_7_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_SUBWORD LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr) RPAREN
  public static boolean built_in_subword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_subword")) return false;
    if (!nextTokenIs(b, BIF_SUBWORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SUBWORD, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_subword_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_SUBWORD, r);
    return r;
  }

  // COMMA parameter_expr
  private static boolean built_in_subword_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_subword_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_SYMBOL LPAREN parameter_expr RPAREN
  public static boolean built_in_symbol(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_symbol")) return false;
    if (!nextTokenIs(b, BIF_SYMBOL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_SYMBOL, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_SYMBOL, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_TIME LPAREN ("N" | parameter_expr)? (COMMA parameter_expr COMMA ("N" | parameter_expr)?)? RPAREN
  public static boolean built_in_time(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time")) return false;
    if (!nextTokenIs(b, BIF_TIME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_TIME, LPAREN);
    r = r && built_in_time_2(b, l + 1);
    r = r && built_in_time_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_TIME, r);
    return r;
  }

  // ("N" | parameter_expr)?
  private static boolean built_in_time_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time_2")) return false;
    built_in_time_2_0(b, l + 1);
    return true;
  }

  // "N" | parameter_expr
  private static boolean built_in_time_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "N");
    if (!r) r = parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA parameter_expr COMMA ("N" | parameter_expr)?)?
  private static boolean built_in_time_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time_3")) return false;
    built_in_time_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr COMMA ("N" | parameter_expr)?
  private static boolean built_in_time_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_time_3_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ("N" | parameter_expr)?
  private static boolean built_in_time_3_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time_3_0_3")) return false;
    built_in_time_3_0_3_0(b, l + 1);
    return true;
  }

  // "N" | parameter_expr
  private static boolean built_in_time_3_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_time_3_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "N");
    if (!r) r = parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_TRACE LPAREN parameter_expr? RPAREN
  public static boolean built_in_trace(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_trace")) return false;
    if (!nextTokenIs(b, BIF_TRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_TRACE, LPAREN);
    r = r && built_in_trace_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_TRACE, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_trace_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_trace_2")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_TRANSLATE LPAREN parameter_expr COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_translate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_translate")) return false;
    if (!nextTokenIs(b, BIF_TRANSLATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_TRANSLATE, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_translate_4(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_translate_6(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_translate_8(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_translate_10(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_translate_12(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_TRANSLATE, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_translate_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_translate_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_translate_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_translate_6")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_translate_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_translate_8")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_translate_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_translate_10")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_translate_12(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_translate_12")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_TRUNC LPAREN parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_trunc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_trunc")) return false;
    if (!nextTokenIs(b, BIF_TRUNC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_TRUNC, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_trunc_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_TRUNC, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_trunc_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_trunc_3")) return false;
    built_in_trunc_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_trunc_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_trunc_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_UPPER LPAREN parameter_expr COMMA parameter_expr? (COMMA parameter_expr)? RPAREN
  public static boolean built_in_upper(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_upper")) return false;
    if (!nextTokenIs(b, BIF_UPPER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_UPPER, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_upper_4(b, l + 1);
    r = r && built_in_upper_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_UPPER, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_upper_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_upper_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_upper_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_upper_5")) return false;
    built_in_upper_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_upper_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_upper_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_USERID LPAREN RPAREN
  public static boolean built_in_userid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_userid")) return false;
    if (!nextTokenIs(b, BIF_USERID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_USERID, LPAREN, RPAREN);
    exit_section_(b, m, BUILT_IN_USERID, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_VALUE LPAREN parameter_expr COMMA parameter_expr? (COMMA parameter_expr) RPAREN
  public static boolean built_in_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_value")) return false;
    if (!nextTokenIs(b, BIF_VALUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_VALUE, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_value_4(b, l + 1);
    r = r && built_in_value_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_VALUE, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_value_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_value_4")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_value_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_value_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_VAR LPAREN parameter_expr RPAREN
  public static boolean built_in_var(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_var")) return false;
    if (!nextTokenIs(b, BIF_VAR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_VAR, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_VAR, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_VERIFY LPAREN parameter_expr COMMA parameter_expr COMMA ("N" | parameter_expr)? COMMA parameter_expr? COMMA parameter_expr? RPAREN
  public static boolean built_in_verify(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_verify")) return false;
    if (!nextTokenIs(b, BIF_VERIFY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_VERIFY, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_verify_6(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_verify_8(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && built_in_verify_10(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_VERIFY, r);
    return r;
  }

  // ("N" | parameter_expr)?
  private static boolean built_in_verify_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_verify_6")) return false;
    built_in_verify_6_0(b, l + 1);
    return true;
  }

  // "N" | parameter_expr
  private static boolean built_in_verify_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_verify_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "N");
    if (!r) r = parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_expr?
  private static boolean built_in_verify_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_verify_8")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  // parameter_expr?
  private static boolean built_in_verify_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_verify_10")) return false;
    parameter_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // BIF_WORD LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_word(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_word")) return false;
    if (!nextTokenIs(b, BIF_WORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_WORD, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_WORD, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_WORDINDEX LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_wordindex(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_wordindex")) return false;
    if (!nextTokenIs(b, BIF_WORDINDEX)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_WORDINDEX, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_WORDINDEX, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_WORDLENGTH LPAREN parameter_expr COMMA parameter_expr RPAREN
  public static boolean built_in_wordlength(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_wordlength")) return false;
    if (!nextTokenIs(b, BIF_WORDLENGTH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_WORDLENGTH, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_WORDLENGTH, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_WORDPOS LPAREN parameter_expr COMMA parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_wordpos(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_wordpos")) return false;
    if (!nextTokenIs(b, BIF_WORDPOS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_WORDPOS, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_wordpos_5(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_WORDPOS, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_wordpos_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_wordpos_5")) return false;
    built_in_wordpos_5_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_wordpos_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_wordpos_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_WORD LPAREN parameter_expr RPAREN
  public static boolean built_in_words(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_words")) return false;
    if (!nextTokenIs(b, BIF_WORD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_WORD, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_WORDS, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_X2B LPAREN parameter_expr RPAREN
  public static boolean built_in_x2b(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_x2b")) return false;
    if (!nextTokenIs(b, BIF_X2B)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_X2B, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_X_2_B, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_X2C LPAREN parameter_expr RPAREN
  public static boolean built_in_x2c(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_x2c")) return false;
    if (!nextTokenIs(b, BIF_X2C)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_X2C, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_X_2_C, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_X2D LPAREN parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_x2d(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_x2d")) return false;
    if (!nextTokenIs(b, BIF_X2D)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_X2D, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_x2d_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_X_2_D, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_x2d_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_x2d_3")) return false;
    built_in_x2d_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_x2d_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_x2d_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BIF_XRANGE LPAREN parameter_expr (COMMA parameter_expr)? RPAREN
  public static boolean built_in_xrange(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_xrange")) return false;
    if (!nextTokenIs(b, BIF_XRANGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, BIF_XRANGE, LPAREN);
    r = r && parameter_expr(b, l + 1);
    r = r && built_in_xrange_3(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, BUILT_IN_XRANGE, r);
    return r;
  }

  // (COMMA parameter_expr)?
  private static boolean built_in_xrange_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_xrange_3")) return false;
    built_in_xrange_3_0(b, l + 1);
    return true;
  }

  // COMMA parameter_expr
  private static boolean built_in_xrange_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "built_in_xrange_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expr (COMMA+ expr)*
  public static boolean comma_separated_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comma_separated_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMMA_SEPARATED_EXPR, "<comma separated expr>");
    r = expr(b, l + 1);
    r = r && comma_separated_expr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA+ expr)*
  private static boolean comma_separated_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comma_separated_expr_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!comma_separated_expr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "comma_separated_expr_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA+ expr
  private static boolean comma_separated_expr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comma_separated_expr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = comma_separated_expr_1_0_0(b, l + 1);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA+
  private static boolean comma_separated_expr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comma_separated_expr_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, COMMA)) break;
      if (!empty_element_parsed_guard_(b, "comma_separated_expr_1_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (EQ EQ) | (LT LT EQ)| (GT GT EQ)| (LT LT) | (GT GT) | (LT GT)
  // | (GT LT) | (NEGATION LT LT) | (NEGATION GT GT) | (LT EQ) | (GT EQ)
  // | (NEGATION EQ EQ) | (NEGATION EQ)| (AND AND) | AND | PIPE  | EQ | LT | GT
  static boolean comparator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = comparator_0(b, l + 1);
    if (!r) r = comparator_1(b, l + 1);
    if (!r) r = comparator_2(b, l + 1);
    if (!r) r = comparator_3(b, l + 1);
    if (!r) r = comparator_4(b, l + 1);
    if (!r) r = comparator_5(b, l + 1);
    if (!r) r = comparator_6(b, l + 1);
    if (!r) r = comparator_7(b, l + 1);
    if (!r) r = comparator_8(b, l + 1);
    if (!r) r = comparator_9(b, l + 1);
    if (!r) r = comparator_10(b, l + 1);
    if (!r) r = comparator_11(b, l + 1);
    if (!r) r = comparator_12(b, l + 1);
    if (!r) r = comparator_13(b, l + 1);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQ EQ
  private static boolean comparator_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, EQ, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT LT EQ
  private static boolean comparator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LT, LT, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // GT GT EQ
  private static boolean comparator_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GT, GT, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT LT
  private static boolean comparator_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LT, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // GT GT
  private static boolean comparator_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GT, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT GT
  private static boolean comparator_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LT, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // GT LT
  private static boolean comparator_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GT, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEGATION LT LT
  private static boolean comparator_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, NEGATION, LT, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEGATION GT GT
  private static boolean comparator_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, NEGATION, GT, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  // LT EQ
  private static boolean comparator_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LT, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // GT EQ
  private static boolean comparator_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GT, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEGATION EQ EQ
  private static boolean comparator_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_11")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, NEGATION, EQ, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEGATION EQ
  private static boolean comparator_12(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_12")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, NEGATION, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // AND AND
  private static boolean comparator_13(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comparator_13")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AND, AND);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !directive_resource_end directive_exclusive_end
  public static boolean directive(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE, "<directive>");
    r = directive_0(b, l + 1);
    r = r && directive_exclusive_end(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !directive_resource_end
  private static boolean directive_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !directive_resource_end(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directive_access_private | directive_access_public
  public static boolean directive_access(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_access")) return false;
    if (!nextTokenIs(b, "<directive access>", PRIVATE, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ACCESS, "<directive access>");
    r = directive_access_private(b, l + 1);
    if (!r) r = directive_access_public(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (PRIVATE)
  public static boolean directive_access_private(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_access_private")) return false;
    if (!nextTokenIs(b, PRIVATE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PRIVATE);
    exit_section_(b, m, DIRECTIVE_ACCESS_PRIVATE, r);
    return r;
  }

  /* ********************************************************** */
  // PUBLIC
  public static boolean directive_access_public(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_access_public")) return false;
    if (!nextTokenIs(b, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PUBLIC);
    exit_section_(b, m, DIRECTIVE_ACCESS_PUBLIC, r);
    return r;
  }

  /* ********************************************************** */
  // ANNOTATE
  //     directive_annotate_reference
  //      directive_annotate_data+ instruction_terminator
  public static boolean directive_annotate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate")) return false;
    if (!nextTokenIs(b, ANNOTATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE, null);
    r = consumeToken(b, ANNOTATE);
    p = r; // pin = 1
    r = r && report_error_(b, directive_annotate_reference(b, l + 1));
    r = p && report_error_(b, directive_annotate_2(b, l + 1)) && r;
    r = p && instruction_terminator(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // directive_annotate_data+
  private static boolean directive_annotate_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_annotate_data(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!directive_annotate_data(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_annotate_2", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ATTRIBUTE directive_annotate_reference_name
  public static boolean directive_annotate_attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_attribute")) return false;
    if (!nextTokenIs(b, ATTRIBUTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_ATTRIBUTE, null);
    r = consumeToken(b, ATTRIBUTE);
    p = r; // pin = 1
    r = r && directive_annotate_reference_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CLASS directive_annotate_reference_name
  public static boolean directive_annotate_class(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_class")) return false;
    if (!nextTokenIs(b, CLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_CLASS, null);
    r = consumeToken(b, CLASS);
    p = r; // pin = 1
    r = r && directive_annotate_reference_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CONSTANT directive_annotate_reference_name
  public static boolean directive_annotate_constant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_constant")) return false;
    if (!nextTokenIs(b, CONSTANT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_CONSTANT, null);
    r = consumeToken(b, CONSTANT);
    p = r; // pin = 1
    r = r && directive_annotate_reference_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // directive_annotate_name directive_annotate_value
  public static boolean directive_annotate_data(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_data")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_DATA, "<directive annotate data>");
    r = directive_annotate_name(b, l + 1);
    r = r && directive_annotate_value(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // METHOD directive_annotate_reference_name
  public static boolean directive_annotate_method(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_method")) return false;
    if (!nextTokenIs(b, METHOD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_METHOD, null);
    r = consumeToken(b, METHOD);
    p = r; // pin = 1
    r = r && directive_annotate_reference_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // id
  public static boolean directive_annotate_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_NAME, "<directive annotate name>");
    r = id(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PACKAGE
  public static boolean directive_annotate_package(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_package")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PACKAGE);
    exit_section_(b, m, DIRECTIVE_ANNOTATE_PACKAGE, r);
    return r;
  }

  /* ********************************************************** */
  // directive_annotate_attribute|directive_annotate_class | directive_annotate_constant | directive_annotate_method | directive_annotate_package | directive_annotate_routine
  public static boolean directive_annotate_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_reference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_REFERENCE, "<directive annotate reference>");
    r = directive_annotate_attribute(b, l + 1);
    if (!r) r = directive_annotate_class(b, l + 1);
    if (!r) r = directive_annotate_constant(b, l + 1);
    if (!r) r = directive_annotate_method(b, l + 1);
    if (!r) r = directive_annotate_package(b, l + 1);
    if (!r) r = directive_annotate_routine(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // id
  public static boolean directive_annotate_reference_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_reference_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_REFERENCE_NAME, "<directive annotate reference name>");
    r = id(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ROUTINE directive_annotate_reference_name
  public static boolean directive_annotate_routine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_routine")) return false;
    if (!nextTokenIs(b, ROUTINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_ROUTINE, null);
    r = consumeToken(b, ROUTINE);
    p = r; // pin = 1
    r = r && directive_annotate_reference_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // lit
  public static boolean directive_annotate_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_annotate_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ANNOTATE_VALUE, "<directive annotate value>");
    r = lit(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ATTRIBUTE symbol_or_string (directive_attribute_get_set | directive_attribute_class
  // | directive_attribute_access | directive_attribute_guard_unguarded
  // | directive_attribute_unprotected_protected
  // | directive_attribute_abstract_delegate_external)* instruction_terminator directive_attribute_body?
  public static boolean directive_attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute")) return false;
    if (!nextTokenIs(b, ATTRIBUTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE, null);
    r = consumeToken(b, ATTRIBUTE);
    p = r; // pin = 1
    r = r && report_error_(b, symbol_or_string(b, l + 1));
    r = p && report_error_(b, directive_attribute_2(b, l + 1)) && r;
    r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
    r = p && directive_attribute_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (directive_attribute_get_set | directive_attribute_class
  // | directive_attribute_access | directive_attribute_guard_unguarded
  // | directive_attribute_unprotected_protected
  // | directive_attribute_abstract_delegate_external)*
  private static boolean directive_attribute_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_attribute_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_attribute_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // directive_attribute_get_set | directive_attribute_class
  // | directive_attribute_access | directive_attribute_guard_unguarded
  // | directive_attribute_unprotected_protected
  // | directive_attribute_abstract_delegate_external
  private static boolean directive_attribute_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_attribute_get_set(b, l + 1);
    if (!r) r = directive_attribute_class(b, l + 1);
    if (!r) r = directive_attribute_access(b, l + 1);
    if (!r) r = directive_attribute_guard_unguarded(b, l + 1);
    if (!r) r = directive_attribute_unprotected_protected(b, l + 1);
    if (!r) r = directive_attribute_abstract_delegate_external(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directive_attribute_body?
  private static boolean directive_attribute_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_4")) return false;
    directive_attribute_body(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ABSTRACT
  public static boolean directive_attribute_abstract(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_abstract")) return false;
    if (!nextTokenIs(b, ABSTRACT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ABSTRACT);
    exit_section_(b, m, DIRECTIVE_ATTRIBUTE_ABSTRACT, r);
    return r;
  }

  /* ********************************************************** */
  // directive_attribute_abstract | directive_attribute_delegate | directive_attribute_external
  public static boolean directive_attribute_abstract_delegate_external(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_abstract_delegate_external")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_ABSTRACT_DELEGATE_EXTERNAL, "<directive attribute abstract delegate external>");
    r = directive_attribute_abstract(b, l + 1);
    if (!r) r = directive_attribute_delegate(b, l + 1);
    if (!r) r = directive_attribute_external(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directive_access
  public static boolean directive_attribute_access(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_access")) return false;
    if (!nextTokenIs(b, "<directive attribute access>", PRIVATE, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_ACCESS, "<directive attribute access>");
    r = directive_access(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (keyword_expose instruction_terminator)? (!keyword_expose instruction)*
  public static boolean directive_attribute_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_BODY, "<directive attribute body>");
    r = directive_attribute_body_0(b, l + 1);
    r = r && directive_attribute_body_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (keyword_expose instruction_terminator)?
  private static boolean directive_attribute_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_body_0")) return false;
    directive_attribute_body_0_0(b, l + 1);
    return true;
  }

  // keyword_expose instruction_terminator
  private static boolean directive_attribute_body_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_body_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_expose(b, l + 1);
    r = r && instruction_terminator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (!keyword_expose instruction)*
  private static boolean directive_attribute_body_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_body_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_attribute_body_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_attribute_body_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // !keyword_expose instruction
  private static boolean directive_attribute_body_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_body_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_attribute_body_1_0_0(b, l + 1);
    r = r && instruction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !keyword_expose
  private static boolean directive_attribute_body_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_body_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !keyword_expose(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CLASS
  public static boolean directive_attribute_class(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_class")) return false;
    if (!nextTokenIs(b, CLASS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CLASS);
    exit_section_(b, m, DIRECTIVE_ATTRIBUTE_CLASS, r);
    return r;
  }

  /* ********************************************************** */
  // DELEGATE symbol_or_string
  public static boolean directive_attribute_delegate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_delegate")) return false;
    if (!nextTokenIs(b, DELEGATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_DELEGATE, null);
    r = consumeToken(b, DELEGATE);
    p = r; // pin = 1
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EXTERNAL string_lit
  public static boolean directive_attribute_external(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_external")) return false;
    if (!nextTokenIs(b, EXTERNAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_EXTERNAL, null);
    r = consumeToken(b, EXTERNAL);
    p = r; // pin = 1
    r = r && string_lit(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // GET | SET
  public static boolean directive_attribute_get_set(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_get_set")) return false;
    if (!nextTokenIs(b, "<directive attribute get set>", GET, SET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_GET_SET, "<directive attribute get set>");
    r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, SET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // GUARDED | UNGUARDED
  public static boolean directive_attribute_guard_unguarded(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_guard_unguarded")) return false;
    if (!nextTokenIs(b, "<directive attribute guard unguarded>", GUARDED, UNGUARDED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_GUARD_UNGUARDED, "<directive attribute guard unguarded>");
    r = consumeToken(b, GUARDED);
    if (!r) r = consumeToken(b, UNGUARDED);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // UNPROTECTED | PROTECTED
  public static boolean directive_attribute_unprotected_protected(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_attribute_unprotected_protected")) return false;
    if (!nextTokenIs(b, "<directive attribute unprotected protected>", PROTECTED, UNPROTECTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ATTRIBUTE_UNPROTECTED_PROTECTED, "<directive attribute unprotected protected>");
    r = consumeToken(b, UNPROTECTED);
    if (!r) r = consumeToken(b, PROTECTED);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CLASS directive_class_name (directive_class_metaclass | directive_class_access | directive_class_mixinclass_subclass | directive_class_abstract)* directive_class_inherit? instruction_terminator
  //                                 directive_class_body?
  public static boolean directive_class(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class")) return false;
    if (!nextTokenIs(b, CLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS, null);
    r = consumeToken(b, CLASS);
    p = r; // pin = 1
    r = r && report_error_(b, directive_class_name(b, l + 1));
    r = p && report_error_(b, directive_class_2(b, l + 1)) && r;
    r = p && report_error_(b, directive_class_3(b, l + 1)) && r;
    r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
    r = p && directive_class_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (directive_class_metaclass | directive_class_access | directive_class_mixinclass_subclass | directive_class_abstract)*
  private static boolean directive_class_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_class_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_class_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // directive_class_metaclass | directive_class_access | directive_class_mixinclass_subclass | directive_class_abstract
  private static boolean directive_class_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_class_metaclass(b, l + 1);
    if (!r) r = directive_class_access(b, l + 1);
    if (!r) r = directive_class_mixinclass_subclass(b, l + 1);
    if (!r) r = directive_class_abstract(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directive_class_inherit?
  private static boolean directive_class_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_3")) return false;
    directive_class_inherit(b, l + 1);
    return true;
  }

  // directive_class_body?
  private static boolean directive_class_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_5")) return false;
    directive_class_body(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ABSTRACT
  public static boolean directive_class_abstract(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_abstract")) return false;
    if (!nextTokenIs(b, ABSTRACT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ABSTRACT);
    exit_section_(b, m, DIRECTIVE_CLASS_ABSTRACT, r);
    return r;
  }

  /* ********************************************************** */
  // directive_access
  public static boolean directive_class_access(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_access")) return false;
    if (!nextTokenIs(b, "<directive class access>", PRIVATE, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_ACCESS, "<directive class access>");
    r = directive_access(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (!(directive_start (directive_class | directive_requires | directive_routine | directive_options)) directive)*
  public static boolean directive_class_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_body")) return false;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_BODY, "<directive class body>");
    int c = current_position_(b);
    while (true) {
      if (!directive_class_body_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_class_body", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // !(directive_start (directive_class | directive_requires | directive_routine | directive_options)) directive
  private static boolean directive_class_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_body_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_class_body_0_0(b, l + 1);
    r = r && directive(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(directive_start (directive_class | directive_requires | directive_routine | directive_options))
  private static boolean directive_class_body_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_body_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !directive_class_body_0_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // directive_start (directive_class | directive_requires | directive_routine | directive_options)
  private static boolean directive_class_body_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_body_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_start(b, l + 1);
    r = r && directive_class_body_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directive_class | directive_requires | directive_routine | directive_options
  private static boolean directive_class_body_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_body_0_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_class(b, l + 1);
    if (!r) r = directive_requires(b, l + 1);
    if (!r) r = directive_routine(b, l + 1);
    if (!r) r = directive_options(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INHERIT directive_class_inherit_name+
  public static boolean directive_class_inherit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_inherit")) return false;
    if (!nextTokenIs(b, INHERIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_INHERIT, null);
    r = consumeToken(b, INHERIT);
    p = r; // pin = 1
    r = r && directive_class_inherit_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // directive_class_inherit_name+
  private static boolean directive_class_inherit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_inherit_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_class_inherit_name(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!directive_class_inherit_name(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_class_inherit_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // symbol_or_string
  public static boolean directive_class_inherit_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_inherit_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_INHERIT_NAME, "<directive class inherit name>");
    r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // METACLASS directive_class_metaclass_name
  public static boolean directive_class_metaclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_metaclass")) return false;
    if (!nextTokenIs(b, METACLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_METACLASS, null);
    r = consumeToken(b, METACLASS);
    p = r; // pin = 1
    r = r && directive_class_metaclass_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // symbol_or_string
  public static boolean directive_class_metaclass_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_metaclass_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_METACLASS_NAME, "<directive class metaclass name>");
    r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // MIXINCLASS directive_class_mixinclass_name
  public static boolean directive_class_mixinclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_mixinclass")) return false;
    if (!nextTokenIs(b, MIXINCLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_MIXINCLASS, null);
    r = consumeToken(b, MIXINCLASS);
    p = r; // pin = 1
    r = r && directive_class_mixinclass_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // symbol_or_string
  public static boolean directive_class_mixinclass_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_mixinclass_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_MIXINCLASS_NAME, "<directive class mixinclass name>");
    r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directive_class_mixinclass | directive_class_subclass
  public static boolean directive_class_mixinclass_subclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_mixinclass_subclass")) return false;
    if (!nextTokenIs(b, "<directive class mixinclass subclass>", MIXINCLASS, SUBCLASS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_MIXINCLASS_SUBCLASS, "<directive class mixinclass subclass>");
    r = directive_class_mixinclass(b, l + 1);
    if (!r) r = directive_class_subclass(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // symbol_or_string
  public static boolean directive_class_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_NAME, "<directive class name>");
    r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SUBCLASS directive_class_subclass_name
  public static boolean directive_class_subclass(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_subclass")) return false;
    if (!nextTokenIs(b, SUBCLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_SUBCLASS, null);
    r = consumeToken(b, SUBCLASS);
    p = r; // pin = 1
    r = r && directive_class_subclass_name(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // symbol_or_string
  public static boolean directive_class_subclass_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_class_subclass_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CLASS_SUBCLASS_NAME, "<directive class subclass name>");
    r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CONSTANT symbol_or_string directive_constant_value instruction_terminator
  public static boolean directive_constant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_constant")) return false;
    if (!nextTokenIs(b, CONSTANT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CONSTANT, null);
    r = consumeToken(b, CONSTANT);
    p = r; // pin = 1
    r = r && report_error_(b, symbol_or_string(b, l + 1));
    r = p && report_error_(b, directive_constant_value(b, l + 1)) && r;
    r = p && instruction_terminator(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // lit?
  public static boolean directive_constant_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_constant_value")) return false;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_CONSTANT_VALUE, "<directive constant value>");
    lit(b, l + 1);
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // directive_start !END (directive_annotate | directive_attribute | directive_class | directive_constant | directive_method | directive_options
  //                             | directive_requires | directive_resource | directive_routine)
  static boolean directive_exclusive_end(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_exclusive_end")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = directive_start(b, l + 1);
    p = r; // pin = 1
    r = r && directive_exclusive_end_1(b, l + 1);
    r = r && directive_exclusive_end_2(b, l + 1);
    exit_section_(b, l, m, r, p, directive_recover_rule_parser_);
    return r || p;
  }

  // !END
  private static boolean directive_exclusive_end_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_exclusive_end_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, END);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // directive_annotate | directive_attribute | directive_class | directive_constant | directive_method | directive_options
  //                             | directive_requires | directive_resource | directive_routine
  private static boolean directive_exclusive_end_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_exclusive_end_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_annotate(b, l + 1);
    if (!r) r = directive_attribute(b, l + 1);
    if (!r) r = directive_class(b, l + 1);
    if (!r) r = directive_constant(b, l + 1);
    if (!r) r = directive_method(b, l + 1);
    if (!r) r = directive_options(b, l + 1);
    if (!r) r = directive_requires(b, l + 1);
    if (!r) r = directive_resource(b, l + 1);
    if (!r) r = directive_routine(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // METHOD directive_method_name (directive_method_attribute | directive_method_class | directive_method_access | directive_method_guarded_unguarded | directive_method_unprotected_protected | directive_method_abstract_delegate_external)* instruction_terminator
  //             directive_method_body?
  public static boolean directive_method(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method")) return false;
    if (!nextTokenIs(b, METHOD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD, null);
    r = consumeToken(b, METHOD);
    p = r; // pin = 1
    r = r && report_error_(b, directive_method_name(b, l + 1));
    r = p && report_error_(b, directive_method_2(b, l + 1)) && r;
    r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
    r = p && directive_method_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (directive_method_attribute | directive_method_class | directive_method_access | directive_method_guarded_unguarded | directive_method_unprotected_protected | directive_method_abstract_delegate_external)*
  private static boolean directive_method_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_method_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_method_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // directive_method_attribute | directive_method_class | directive_method_access | directive_method_guarded_unguarded | directive_method_unprotected_protected | directive_method_abstract_delegate_external
  private static boolean directive_method_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_method_attribute(b, l + 1);
    if (!r) r = directive_method_class(b, l + 1);
    if (!r) r = directive_method_access(b, l + 1);
    if (!r) r = directive_method_guarded_unguarded(b, l + 1);
    if (!r) r = directive_method_unprotected_protected(b, l + 1);
    if (!r) r = directive_method_abstract_delegate_external(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directive_method_body?
  private static boolean directive_method_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_4")) return false;
    directive_method_body(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ABSTRACT
  public static boolean directive_method_abstract(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_abstract")) return false;
    if (!nextTokenIs(b, ABSTRACT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ABSTRACT);
    exit_section_(b, m, DIRECTIVE_METHOD_ABSTRACT, r);
    return r;
  }

  /* ********************************************************** */
  // directive_method_abstract | directive_method_external | directive_method_delegate
  public static boolean directive_method_abstract_delegate_external(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_abstract_delegate_external")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_ABSTRACT_DELEGATE_EXTERNAL, "<directive method abstract delegate external>");
    r = directive_method_abstract(b, l + 1);
    if (!r) r = directive_method_external(b, l + 1);
    if (!r) r = directive_method_delegate(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directive_access
  public static boolean directive_method_access(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_access")) return false;
    if (!nextTokenIs(b, "<directive method access>", PRIVATE, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_ACCESS, "<directive method access>");
    r = directive_access(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ATTRIBUTE
  public static boolean directive_method_attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_attribute")) return false;
    if (!nextTokenIs(b, ATTRIBUTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ATTRIBUTE);
    exit_section_(b, m, DIRECTIVE_METHOD_ATTRIBUTE, r);
    return r;
  }

  /* ********************************************************** */
  // (keyword_expose instruction_terminator)? (!keyword_expose instruction)*
  public static boolean directive_method_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_BODY, "<directive method body>");
    r = directive_method_body_0(b, l + 1);
    r = r && directive_method_body_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (keyword_expose instruction_terminator)?
  private static boolean directive_method_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_body_0")) return false;
    directive_method_body_0_0(b, l + 1);
    return true;
  }

  // keyword_expose instruction_terminator
  private static boolean directive_method_body_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_body_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_expose(b, l + 1);
    r = r && instruction_terminator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (!keyword_expose instruction)*
  private static boolean directive_method_body_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_body_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_method_body_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_method_body_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // !keyword_expose instruction
  private static boolean directive_method_body_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_body_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_method_body_1_0_0(b, l + 1);
    r = r && instruction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !keyword_expose
  private static boolean directive_method_body_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_body_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !keyword_expose(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CLASS
  public static boolean directive_method_class(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_class")) return false;
    if (!nextTokenIs(b, CLASS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CLASS);
    exit_section_(b, m, DIRECTIVE_METHOD_CLASS, r);
    return r;
  }

  /* ********************************************************** */
  // DELEGATE symbol_or_string
  public static boolean directive_method_delegate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_delegate")) return false;
    if (!nextTokenIs(b, DELEGATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_DELEGATE, null);
    r = consumeToken(b, DELEGATE);
    p = r; // pin = 1
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EXTERNAL string_lit
  public static boolean directive_method_external(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_external")) return false;
    if (!nextTokenIs(b, EXTERNAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_EXTERNAL, null);
    r = consumeToken(b, EXTERNAL);
    p = r; // pin = 1
    r = r && string_lit(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // GUARDED | UNGUARDED
  public static boolean directive_method_guarded_unguarded(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_guarded_unguarded")) return false;
    if (!nextTokenIs(b, "<directive method guarded unguarded>", GUARDED, UNGUARDED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_GUARDED_UNGUARDED, "<directive method guarded unguarded>");
    r = consumeToken(b, GUARDED);
    if (!r) r = consumeToken(b, UNGUARDED);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // symbol_or_string
  public static boolean directive_method_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_name")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_NAME, "<directive method name>");
    r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // UNPROTECTED | PROTECTED
  public static boolean directive_method_unprotected_protected(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_method_unprotected_protected")) return false;
    if (!nextTokenIs(b, "<directive method unprotected protected>", PROTECTED, UNPROTECTED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_METHOD_UNPROTECTED_PROTECTED, "<directive method unprotected protected>");
    r = consumeToken(b, UNPROTECTED);
    if (!r) r = consumeToken(b, PROTECTED);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OPTIONS directive_options_key_value_pair* instruction_terminator
  public static boolean directive_options(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options")) return false;
    if (!nextTokenIs(b, OPTIONS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_OPTIONS, null);
    r = consumeToken(b, OPTIONS);
    p = r; // pin = 1
    r = r && report_error_(b, directive_options_1(b, l + 1));
    r = p && instruction_terminator(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // directive_options_key_value_pair*
  private static boolean directive_options_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_options_key_value_pair(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_options_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // (DIGITS INTEGER_LITERAL) | (FORM (ENGINEERING | SCIENTIFIC)) | (FUZZ id) |
  //                                      (NOVALUE (CONDITION | ERROR)) | PROLOG | NOPROLOG | (TRACE id)
  public static boolean directive_options_key_value_pair(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_OPTIONS_KEY_VALUE_PAIR, "<directive options key value pair>");
    r = directive_options_key_value_pair_0(b, l + 1);
    if (!r) r = directive_options_key_value_pair_1(b, l + 1);
    if (!r) r = directive_options_key_value_pair_2(b, l + 1);
    if (!r) r = directive_options_key_value_pair_3(b, l + 1);
    if (!r) r = consumeToken(b, PROLOG);
    if (!r) r = consumeToken(b, NOPROLOG);
    if (!r) r = directive_options_key_value_pair_6(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DIGITS INTEGER_LITERAL
  private static boolean directive_options_key_value_pair_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DIGITS, INTEGER_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // FORM (ENGINEERING | SCIENTIFIC)
  private static boolean directive_options_key_value_pair_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FORM);
    r = r && directive_options_key_value_pair_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ENGINEERING | SCIENTIFIC
  private static boolean directive_options_key_value_pair_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ENGINEERING);
    if (!r) r = consumeToken(b, SCIENTIFIC);
    exit_section_(b, m, null, r);
    return r;
  }

  // FUZZ id
  private static boolean directive_options_key_value_pair_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FUZZ);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NOVALUE (CONDITION | ERROR)
  private static boolean directive_options_key_value_pair_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOVALUE);
    r = r && directive_options_key_value_pair_3_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CONDITION | ERROR
  private static boolean directive_options_key_value_pair_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_3_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONDITION);
    if (!r) r = consumeToken(b, ERROR);
    exit_section_(b, m, null, r);
    return r;
  }

  // TRACE id
  private static boolean directive_options_key_value_pair_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_options_key_value_pair_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TRACE);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(directive_start)
  static boolean directive_recover_rule(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_recover_rule")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !directive_recover_rule_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (directive_start)
  private static boolean directive_recover_rule_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_recover_rule_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_start(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // REQUIRES directive_requires_path instruction_terminator
  public static boolean directive_requires(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_requires")) return false;
    if (!nextTokenIs(b, REQUIRES)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_REQUIRES, null);
    r = consumeToken(b, REQUIRES);
    p = r; // pin = 1
    r = r && report_error_(b, directive_requires_path(b, l + 1));
    r = p && instruction_terminator(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (id | STRING_LITERAL) (LIBRARY | (NAMESPACE id))?
  public static boolean directive_requires_path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_requires_path")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_REQUIRES_PATH, "<directive requires path>");
    r = directive_requires_path_0(b, l + 1);
    r = r && directive_requires_path_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // id | STRING_LITERAL
  private static boolean directive_requires_path_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_requires_path_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = id(b, l + 1);
    if (!r) r = consumeToken(b, STRING_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LIBRARY | (NAMESPACE id))?
  private static boolean directive_requires_path_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_requires_path_1")) return false;
    directive_requires_path_1_0(b, l + 1);
    return true;
  }

  // LIBRARY | (NAMESPACE id)
  private static boolean directive_requires_path_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_requires_path_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LIBRARY);
    if (!r) r = directive_requires_path_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NAMESPACE id
  private static boolean directive_requires_path_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_requires_path_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAMESPACE);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // directive_resource_with_delemiter | directive_resource_end_directive
  public static boolean directive_resource(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_RESOURCE, "<directive resource>");
    r = directive_resource_with_delemiter_ext(b, l + 1);
    if (!r) r = directive_resource_end_directive(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directive_resource_data_ext_call
  public static boolean directive_resource_data(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_data")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_RESOURCE_DATA, "<directive resource data>");
    r = directive_resource_data_ext(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // directive_start END
  public static boolean directive_resource_end(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_start(b, l + 1);
    r = r && consumeToken(b, END);
    exit_section_(b, m, DIRECTIVE_RESOURCE_END, r);
    return r;
  }

  /* ********************************************************** */
  // "RESOURCE" (!(END END)symbol_or_string_or_keyword) instruction_terminator (!<<eof>> !directive_resource_end directive_resource_data)* directive_resource_end instruction_terminator
  static boolean directive_resource_end_directive(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive")) return false;
    if (!nextTokenIs(b, RESOURCE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, RESOURCE);
    r = r && directive_resource_end_directive_1(b, l + 1);
    r = r && instruction_terminator(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, directive_resource_end_directive_3(b, l + 1));
    r = p && report_error_(b, directive_resource_end(b, l + 1)) && r;
    r = p && instruction_terminator(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !(END END)symbol_or_string_or_keyword
  private static boolean directive_resource_end_directive_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_resource_end_directive_1_0(b, l + 1);
    r = r && symbol_or_string_or_keyword(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(END END)
  private static boolean directive_resource_end_directive_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !directive_resource_end_directive_1_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // END END
  private static boolean directive_resource_end_directive_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, END, END);
    exit_section_(b, m, null, r);
    return r;
  }

  // (!<<eof>> !directive_resource_end directive_resource_data)*
  private static boolean directive_resource_end_directive_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_resource_end_directive_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_resource_end_directive_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // !<<eof>> !directive_resource_end directive_resource_data
  private static boolean directive_resource_end_directive_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_resource_end_directive_3_0_0(b, l + 1);
    r = r && directive_resource_end_directive_3_0_1(b, l + 1);
    r = r && directive_resource_data(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !<<eof>>
  private static boolean directive_resource_end_directive_3_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_3_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !eof(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !directive_resource_end
  private static boolean directive_resource_end_directive_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_resource_end_directive_3_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !directive_resource_end(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ROUTINE symbol_or_string (directive_routine_private_public | directive_routine_external)* instruction_terminator directive_routine_body?
  public static boolean directive_routine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine")) return false;
    if (!nextTokenIs(b, ROUTINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ROUTINE, null);
    r = consumeToken(b, ROUTINE);
    p = r; // pin = 1
    r = r && report_error_(b, symbol_or_string(b, l + 1));
    r = p && report_error_(b, directive_routine_2(b, l + 1)) && r;
    r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
    r = p && directive_routine_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (directive_routine_private_public | directive_routine_external)*
  private static boolean directive_routine_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!directive_routine_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_routine_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // directive_routine_private_public | directive_routine_external
  private static boolean directive_routine_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_routine_private_public(b, l + 1);
    if (!r) r = directive_routine_external(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // directive_routine_body?
  private static boolean directive_routine_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_4")) return false;
    directive_routine_body(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (!keyword_expose instruction)+ | instruction_terminator
  public static boolean directive_routine_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_body")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ROUTINE_BODY, "<directive routine body>");
    r = directive_routine_body_0(b, l + 1);
    if (!r) r = instruction_terminator(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (!keyword_expose instruction)+
  private static boolean directive_routine_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_body_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_routine_body_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!directive_routine_body_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "directive_routine_body_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // !keyword_expose instruction
  private static boolean directive_routine_body_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_body_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive_routine_body_0_0_0(b, l + 1);
    r = r && instruction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !keyword_expose
  private static boolean directive_routine_body_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_body_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !keyword_expose(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EXTERNAL symbol_or_string
  public static boolean directive_routine_external(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_external")) return false;
    if (!nextTokenIs(b, EXTERNAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ROUTINE_EXTERNAL, null);
    r = consumeToken(b, EXTERNAL);
    p = r; // pin = 1
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // directive_access
  public static boolean directive_routine_private_public(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_routine_private_public")) return false;
    if (!nextTokenIs(b, "<directive routine private public>", PRIVATE, PUBLIC)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DIRECTIVE_ROUTINE_PRIVATE_PUBLIC, "<directive routine private public>");
    r = directive_access(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COLON COLON
  public static boolean directive_start(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "directive_start")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COLON, COLON);
    exit_section_(b, m, DIRECTIVE_START, r);
    return r;
  }

  /* ********************************************************** */
  // ENV_IDENTIFIER | ENV_ENVIRONMENT | ENV_LOCAL
  public static boolean environment_id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "environment_id")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ENVIRONMENT_ID, "<environment id>");
    r = consumeToken(b, ENV_IDENTIFIER);
    if (!r) r = consumeToken(b, ENV_ENVIRONMENT);
    if (!r) r = consumeToken(b, ENV_LOCAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<meta_expr (<<p>>)>> | (LPAREN (expr) RPAREN !<<p>>)
  public static boolean excluding_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "excluding_expr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = meta_expr(b, l + 1, new Parser() {
      public boolean parse(PsiBuilder b, int l) {
        return excluding_expr_0_0(b, l + 1, _p);
      }
    });
    if (!r) r = excluding_expr_1(b, l + 1, _p);
    exit_section_(b, m, EXCLUDING_EXPR, r);
    return r;
  }

  // <<p>>
  private static boolean excluding_expr_0_0(PsiBuilder b, int l, Parser _p) {
    return _p.parse(b, l);
  }

  // LPAREN (expr) RPAREN !<<p>>
  private static boolean excluding_expr_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "excluding_expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && excluding_expr_1_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    r = r && excluding_expr_1_3(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // (expr)
  private static boolean excluding_expr_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "excluding_expr_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !<<p>>
  private static boolean excluding_expr_1_3(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "excluding_expr_1_3")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !_p.parse(b, l);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<meta_expr DUMMYOBJECT>>
  public static boolean expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR, "<expr>");
    r = meta_expr(b, l + 1, DUMMYOBJECT_parser_);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // NEGATION? <<summand <<p>>>> message_term_tail* <<bool_expr <<p>>>> *
  static boolean expr_raw(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "expr_raw")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr_raw_0(b, l + 1);
    r = r && summand(b, l + 1, _p);
    r = r && expr_raw_2(b, l + 1);
    r = r && expr_raw_3(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEGATION?
  private static boolean expr_raw_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_raw_0")) return false;
    consumeToken(b, NEGATION);
    return true;
  }

  // message_term_tail*
  private static boolean expr_raw_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_raw_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!message_term_tail(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_raw_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // <<bool_expr <<p>>>> *
  private static boolean expr_raw_3(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "expr_raw_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!bool_expr(b, l + 1, _p)) break;
      if (!empty_element_parsed_guard_(b, "expr_raw_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // EQ  | ( (PLUS | MINUS | (MUL MUL) | MUL | (DIV DIV) | DIV | INTDIV | (PIPE PIPE) | PIPE | (AND AND) | AND) EQ)
  public static boolean extended_assignment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXTENDED_ASSIGNMENT, "<extended assignment>");
    r = consumeToken(b, EQ);
    if (!r) r = extended_assignment_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (PLUS | MINUS | (MUL MUL) | MUL | (DIV DIV) | DIV | INTDIV | (PIPE PIPE) | PIPE | (AND AND) | AND) EQ
  private static boolean extended_assignment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = extended_assignment_1_0(b, l + 1);
    r = r && consumeToken(b, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // PLUS | MINUS | (MUL MUL) | MUL | (DIV DIV) | DIV | INTDIV | (PIPE PIPE) | PIPE | (AND AND) | AND
  private static boolean extended_assignment_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = extended_assignment_1_0_2(b, l + 1);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = extended_assignment_1_0_4(b, l + 1);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, INTDIV);
    if (!r) r = extended_assignment_1_0_7(b, l + 1);
    if (!r) r = consumeToken(b, PIPE);
    if (!r) r = extended_assignment_1_0_9(b, l + 1);
    if (!r) r = consumeToken(b, AND);
    exit_section_(b, m, null, r);
    return r;
  }

  // MUL MUL
  private static boolean extended_assignment_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment_1_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MUL, MUL);
    exit_section_(b, m, null, r);
    return r;
  }

  // DIV DIV
  private static boolean extended_assignment_1_0_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment_1_0_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DIV, DIV);
    exit_section_(b, m, null, r);
    return r;
  }

  // PIPE PIPE
  private static boolean extended_assignment_1_0_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment_1_0_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PIPE, PIPE);
    exit_section_(b, m, null, r);
    return r;
  }

  // AND AND
  private static boolean extended_assignment_1_0_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "extended_assignment_1_0_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AND, AND);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NEGATION? <<primary <<p>>>> <<mul_expr <<p>>>> *
  static boolean factor(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "factor")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = factor_0(b, l + 1);
    r = r && primary(b, l + 1, _p);
    r = r && factor_2(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // NEGATION?
  private static boolean factor_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factor_0")) return false;
    consumeToken(b, NEGATION);
    return true;
  }

  // <<mul_expr <<p>>>> *
  private static boolean factor_2(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "factor_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!mul_expr(b, l + 1, _p)) break;
      if (!empty_element_parsed_guard_(b, "factor_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // built_in_functions | (namespace_prefix? function_call_without_namespace)
  public static boolean function_call(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_CALL, "<function call>");
    r = built_in_functions(b, l + 1);
    if (!r) r = function_call_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // namespace_prefix? function_call_without_namespace
  private static boolean function_call_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call_1_0(b, l + 1);
    r = r && function_call_without_namespace(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // namespace_prefix?
  private static boolean function_call_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_1_0")) return false;
    namespace_prefix(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (FUNC_IDENTIFIER | built_in_functions_keyword_list) LPAREN (COMMA | parameter_expr)* RPAREN
  static boolean function_call_without_namespace(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_without_namespace")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = function_call_without_namespace_0(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    p = r; // pin = 2
    r = r && report_error_(b, function_call_without_namespace_2(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // FUNC_IDENTIFIER | built_in_functions_keyword_list
  private static boolean function_call_without_namespace_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_without_namespace_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FUNC_IDENTIFIER);
    if (!r) r = built_in_functions_keyword_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA | parameter_expr)*
  private static boolean function_call_without_namespace_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_without_namespace_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!function_call_without_namespace_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "function_call_without_namespace_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA | parameter_expr
  private static boolean function_call_without_namespace_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_without_namespace_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = parameter_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (SELF TILDE)? (namespace_prefix (new_line* !assignments_and_keywords))? (identifier | environment_id | keyword_list)
  public static boolean id(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ID, "<id>");
    r = id_0(b, l + 1);
    r = r && id_1(b, l + 1);
    r = r && id_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SELF TILDE)?
  private static boolean id_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_0")) return false;
    id_0_0(b, l + 1);
    return true;
  }

  // SELF TILDE
  private static boolean id_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, SELF, TILDE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (namespace_prefix (new_line* !assignments_and_keywords))?
  private static boolean id_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_1")) return false;
    id_1_0(b, l + 1);
    return true;
  }

  // namespace_prefix (new_line* !assignments_and_keywords)
  private static boolean id_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namespace_prefix(b, l + 1);
    r = r && id_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // new_line* !assignments_and_keywords
  private static boolean id_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = id_1_0_1_0(b, l + 1);
    r = r && id_1_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // new_line*
  private static boolean id_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_1_0_1_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!new_line(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "id_1_0_1_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // !assignments_and_keywords
  private static boolean id_1_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_1_0_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !assignments_and_keywords(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // identifier | environment_id | keyword_list
  private static boolean id_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = environment_id(b, l + 1);
    if (!r) r = keyword_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // id | lit
  public static boolean id_with_lit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_with_lit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ID_WITH_LIT, "<id with lit>");
    r = id(b, l + 1);
    if (!r) r = lit(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // id | number_lit
  public static boolean id_with_number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "id_with_number")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ID_WITH_NUMBER, "<id with number>");
    r = id(b, l + 1);
    if (!r) r = number_lit(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SEMICOLON | label_instruction | ((assignments_and_keywords | (!END keyword_list))  instruction_terminator)
  public static boolean instruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INSTRUCTION, "<instruction>");
    r = consumeToken(b, SEMICOLON);
    if (!r) r = label_instruction(b, l + 1);
    if (!r) r = instruction_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (assignments_and_keywords | (!END keyword_list))  instruction_terminator
  private static boolean instruction_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instruction_2_0(b, l + 1);
    r = r && instruction_terminator(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // assignments_and_keywords | (!END keyword_list)
  private static boolean instruction_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignments_and_keywords(b, l + 1);
    if (!r) r = instruction_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !END keyword_list
  private static boolean instruction_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instruction_2_0_1_0(b, l + 1);
    r = r && keyword_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !END
  private static boolean instruction_2_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_2_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, END);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EOL+ | (SEMICOLON+ new_line*) | <<eof>>
  public static boolean instruction_terminator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_terminator")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INSTRUCTION_TERMINATOR, "<instruction terminator>");
    r = instruction_terminator_0(b, l + 1);
    if (!r) r = instruction_terminator_1(b, l + 1);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EOL+
  private static boolean instruction_terminator_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_terminator_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EOL);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, EOL)) break;
      if (!empty_element_parsed_guard_(b, "instruction_terminator_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // SEMICOLON+ new_line*
  private static boolean instruction_terminator_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_terminator_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instruction_terminator_1_0(b, l + 1);
    r = r && instruction_terminator_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SEMICOLON+
  private static boolean instruction_terminator_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_terminator_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMICOLON);
    int c = current_position_(b);
    while (r) {
      if (!consumeToken(b, SEMICOLON)) break;
      if (!empty_element_parsed_guard_(b, "instruction_terminator_1_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // new_line*
  private static boolean instruction_terminator_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "instruction_terminator_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!new_line(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "instruction_terminator_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // ADDRESS ((symbol_or_string expr?) | (VALUE? expr) )
  public static boolean keyword_address(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_address")) return false;
    if (!nextTokenIs(b, ADDRESS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_ADDRESS, null);
    r = consumeToken(b, ADDRESS);
    p = r; // pin = 1
    r = r && keyword_address_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (symbol_or_string expr?) | (VALUE? expr)
  private static boolean keyword_address_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_address_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_address_1_0(b, l + 1);
    if (!r) r = keyword_address_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // symbol_or_string expr?
  private static boolean keyword_address_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_address_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_or_string(b, l + 1);
    r = r && keyword_address_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expr?
  private static boolean keyword_address_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_address_1_0_1")) return false;
    expr(b, l + 1);
    return true;
  }

  // VALUE? expr
  private static boolean keyword_address_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_address_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_address_1_1_0(b, l + 1);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE?
  private static boolean keyword_address_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_address_1_1_0")) return false;
    consumeToken(b, VALUE);
    return true;
  }

  /* ********************************************************** */
  // ARG template_list?
  public static boolean keyword_arg(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_arg")) return false;
    if (!nextTokenIs(b, ARG)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_ARG, null);
    r = consumeToken(b, ARG);
    p = r; // pin = 1
    r = r && keyword_arg_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // template_list?
  private static boolean keyword_arg_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_arg_1")) return false;
    template_list(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ARG | LINEIN | PULL | SOURCE | VAR (symbol_or_string) | VERSION | (VALUE <<excluding_expr WITH>> WITH)
  static boolean keyword_arg_etc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_arg_etc")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARG);
    if (!r) r = consumeToken(b, LINEIN);
    if (!r) r = consumeToken(b, PULL);
    if (!r) r = consumeToken(b, SOURCE);
    if (!r) r = keyword_arg_etc_4(b, l + 1);
    if (!r) r = consumeToken(b, VERSION);
    if (!r) r = keyword_arg_etc_6(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VAR (symbol_or_string)
  private static boolean keyword_arg_etc_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_arg_etc_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VAR);
    r = r && keyword_arg_etc_4_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (symbol_or_string)
  private static boolean keyword_arg_etc_4_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_arg_etc_4_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_or_string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE <<excluding_expr WITH>> WITH
  private static boolean keyword_arg_etc_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_arg_etc_6")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VALUE);
    r = r && excluding_expr(b, l + 1, WITH_parser_);
    r = r && consumeToken(b, WITH);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CALL (symbol_or_string_or_function | ( LPAREN expr RPAREN)) comma_separated_expr?
  public static boolean keyword_call(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call")) return false;
    if (!nextTokenIs(b, CALL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_CALL, null);
    r = consumeToken(b, CALL);
    p = r; // pin = 1
    r = r && report_error_(b, keyword_call_1(b, l + 1));
    r = p && keyword_call_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // symbol_or_string_or_function | ( LPAREN expr RPAREN)
  private static boolean keyword_call_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_or_string_or_function(b, l + 1);
    if (!r) r = keyword_call_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expr RPAREN
  private static boolean keyword_call_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // comma_separated_expr?
  private static boolean keyword_call_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_2")) return false;
    comma_separated_expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // CALL OFF (ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id))
  public static boolean keyword_call_off(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_off")) return false;
    if (!nextTokenIs(b, CALL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_CALL_OFF, null);
    r = consumeTokens(b, 1, CALL, OFF);
    p = r; // pin = 1
    r = r && keyword_call_off_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)
  private static boolean keyword_call_off_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_off_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = keyword_call_off_2_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // USER id
  private static boolean keyword_call_off_2_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_off_2_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, USER);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CALL ON (((ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)) (NAME id)) |
  //                             ((NAME id) (ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id))))
  public static boolean keyword_call_on(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on")) return false;
    if (!nextTokenIs(b, CALL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_CALL_ON, null);
    r = consumeTokens(b, 1, CALL, ON);
    p = r; // pin = 1
    r = r && keyword_call_on_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ((ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)) (NAME id)) |
  //                             ((NAME id) (ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)))
  private static boolean keyword_call_on_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_call_on_2_0(b, l + 1);
    if (!r) r = keyword_call_on_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)) (NAME id)
  private static boolean keyword_call_on_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_call_on_2_0_0(b, l + 1);
    r = r && keyword_call_on_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)
  private static boolean keyword_call_on_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = keyword_call_on_2_0_0_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // USER id
  private static boolean keyword_call_on_2_0_0_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_0_0_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, USER);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NAME id
  private static boolean keyword_call_on_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAME);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (NAME id) (ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id))
  private static boolean keyword_call_on_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_call_on_2_1_0(b, l + 1);
    r = r && keyword_call_on_2_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // NAME id
  private static boolean keyword_call_on_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAME);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ANY | ERROR | FAILURE | HALT | NOTREADY | (USER id)
  private static boolean keyword_call_on_2_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = keyword_call_on_2_1_1_5(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // USER id
  private static boolean keyword_call_on_2_1_1_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_on_2_1_1_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, USER);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // keyword_call | keyword_call_on | keyword_call_off
  public static boolean keyword_call_variants(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_call_variants")) return false;
    if (!nextTokenIs(b, CALL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_call(b, l + 1);
    if (!r) r = keyword_call_on(b, l + 1);
    if (!r) r = keyword_call_off(b, l + 1);
    exit_section_(b, m, KEYWORD_CALL_VARIANTS, r);
    return r;
  }

  /* ********************************************************** */
  // (DO | LOOP) (LABEL id)?
  //     (
  //         (keyword_do_conditional  keyword_do_repetitor) |
  //         (keyword_do_repetitor  keyword_do_conditional) |
  //         (keyword_do_conditional) |
  //         (keyword_do_repetitor)
  //     )?
  // instruction_terminator instruction* END (id)?
  public static boolean keyword_do(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do")) return false;
    if (!nextTokenIs(b, "<keyword do>", DO, LOOP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_DO, "<keyword do>");
    r = keyword_do_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, keyword_do_1(b, l + 1));
    r = p && report_error_(b, keyword_do_2(b, l + 1)) && r;
    r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
    r = p && report_error_(b, keyword_do_4(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, END)) && r;
    r = p && keyword_do_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // DO | LOOP
  private static boolean keyword_do_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, LOOP);
    exit_section_(b, m, null, r);
    return r;
  }

  // (LABEL id)?
  private static boolean keyword_do_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_1")) return false;
    keyword_do_1_0(b, l + 1);
    return true;
  }

  // LABEL id
  private static boolean keyword_do_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LABEL);
    r = r && id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (
  //         (keyword_do_conditional  keyword_do_repetitor) |
  //         (keyword_do_repetitor  keyword_do_conditional) |
  //         (keyword_do_conditional) |
  //         (keyword_do_repetitor)
  //     )?
  private static boolean keyword_do_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_2")) return false;
    keyword_do_2_0(b, l + 1);
    return true;
  }

  // (keyword_do_conditional  keyword_do_repetitor) |
  //         (keyword_do_repetitor  keyword_do_conditional) |
  //         (keyword_do_conditional) |
  //         (keyword_do_repetitor)
  private static boolean keyword_do_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_2_0_0(b, l + 1);
    if (!r) r = keyword_do_2_0_1(b, l + 1);
    if (!r) r = keyword_do_2_0_2(b, l + 1);
    if (!r) r = keyword_do_2_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyword_do_conditional  keyword_do_repetitor
  private static boolean keyword_do_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_2_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_conditional(b, l + 1);
    r = r && keyword_do_repetitor(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyword_do_repetitor  keyword_do_conditional
  private static boolean keyword_do_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_repetitor(b, l + 1);
    r = r && keyword_do_conditional(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_do_conditional)
  private static boolean keyword_do_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_2_0_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_conditional(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_do_repetitor)
  private static boolean keyword_do_2_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_2_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_repetitor(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // instruction*
  private static boolean keyword_do_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instruction(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_do_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (id)?
  private static boolean keyword_do_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_6")) return false;
    keyword_do_6_0(b, l + 1);
    return true;
  }

  // (id)
  private static boolean keyword_do_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (WHILE | UNTIL) ( <<excluding_expr (TO | BY | INDEX | ITEM | FOR)>> | COMMA)+
  public static boolean keyword_do_conditional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_conditional")) return false;
    if (!nextTokenIs(b, "<keyword do conditional>", UNTIL, WHILE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_DO_CONDITIONAL, "<keyword do conditional>");
    r = keyword_do_conditional_0(b, l + 1);
    r = r && keyword_do_conditional_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // WHILE | UNTIL
  private static boolean keyword_do_conditional_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_conditional_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, UNTIL);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<excluding_expr (TO | BY | INDEX | ITEM | FOR)>> | COMMA)+
  private static boolean keyword_do_conditional_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_conditional_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_conditional_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!keyword_do_conditional_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_do_conditional_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // <<excluding_expr (TO | BY | INDEX | ITEM | FOR)>> | COMMA
  private static boolean keyword_do_conditional_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_conditional_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = excluding_expr(b, l + 1, keyword_do_conditional_1_0_0_0_parser_);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  // TO | BY | INDEX | ITEM | FOR
  private static boolean keyword_do_conditional_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_conditional_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, BY);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, ITEM);
    if (!r) r = consumeToken(b, FOR);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // keyword_do_repetitor_1 | keyword_do_repetitor_2 | keyword_do_repetitor_3 | FOREVER | expr
  public static boolean keyword_do_repetitor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_DO_REPETITOR, "<keyword do repetitor>");
    r = keyword_do_repetitor_1(b, l + 1);
    if (!r) r = keyword_do_repetitor_2(b, l + 1);
    if (!r) r = keyword_do_repetitor_3(b, l + 1);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // keyword_do_repetitor_assign (keyword_do_repetitor_to | keyword_do_repetitor_by | keyword_do_repetitor_for)*
  static boolean keyword_do_repetitor_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_repetitor_assign(b, l + 1);
    r = r && keyword_do_repetitor_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_do_repetitor_to | keyword_do_repetitor_by | keyword_do_repetitor_for)*
  private static boolean keyword_do_repetitor_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_do_repetitor_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_do_repetitor_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_do_repetitor_to | keyword_do_repetitor_by | keyword_do_repetitor_for
  private static boolean keyword_do_repetitor_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_repetitor_to(b, l + 1);
    if (!r) r = keyword_do_repetitor_by(b, l + 1);
    if (!r) r = keyword_do_repetitor_for(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (id OVER <<excluding_expr FOR>>) keyword_do_repetitor_2_for?
  static boolean keyword_do_repetitor_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_repetitor_2_0(b, l + 1);
    r = r && keyword_do_repetitor_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // id OVER <<excluding_expr FOR>>
  private static boolean keyword_do_repetitor_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = id(b, l + 1);
    r = r && consumeToken(b, OVER);
    r = r && excluding_expr(b, l + 1, FOR_parser_);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyword_do_repetitor_2_for?
  private static boolean keyword_do_repetitor_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_2_1")) return false;
    keyword_do_repetitor_2_for(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FOR <<excluding_expr (WHILE | UNTIL)>>
  static boolean keyword_do_repetitor_2_for(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_2_for")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && excluding_expr(b, l + 1, keyword_do_repetitor_2_for_1_0_parser_);
    exit_section_(b, m, null, r);
    return r;
  }

  // WHILE | UNTIL
  private static boolean keyword_do_repetitor_2_for_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_2_for_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, UNTIL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // WITH (keyword_do_repetitor_index | keyword_do_repetitor_item)* OVER id keyword_do_repetitor_for?
  static boolean keyword_do_repetitor_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_3")) return false;
    if (!nextTokenIs(b, WITH)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WITH);
    r = r && keyword_do_repetitor_3_1(b, l + 1);
    r = r && consumeToken(b, OVER);
    r = r && id(b, l + 1);
    r = r && keyword_do_repetitor_3_4(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_do_repetitor_index | keyword_do_repetitor_item)*
  private static boolean keyword_do_repetitor_3_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_3_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_do_repetitor_3_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_do_repetitor_3_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_do_repetitor_index | keyword_do_repetitor_item
  private static boolean keyword_do_repetitor_3_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_3_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_do_repetitor_index(b, l + 1);
    if (!r) r = keyword_do_repetitor_item(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyword_do_repetitor_for?
  private static boolean keyword_do_repetitor_3_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_3_4")) return false;
    keyword_do_repetitor_for(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // id EQ <<excluding_expr (TO | BY | INDEX | ITEM | FOR | WHILE | UNTIL)>>
  public static boolean keyword_do_repetitor_assign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_assign")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_DO_REPETITOR_ASSIGN, "<keyword do repetitor assign>");
    r = id(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && excluding_expr(b, l + 1, keyword_do_repetitor_assign_2_0_parser_);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TO | BY | INDEX | ITEM | FOR | WHILE | UNTIL
  private static boolean keyword_do_repetitor_assign_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_assign_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, BY);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, ITEM);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, UNTIL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BY <<excluding_expr (TO | FOR | INDEX | ITEM | WHILE | UNTIL)>>
  public static boolean keyword_do_repetitor_by(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_by")) return false;
    if (!nextTokenIs(b, BY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BY);
    r = r && excluding_expr(b, l + 1, keyword_do_repetitor_by_1_0_parser_);
    exit_section_(b, m, KEYWORD_DO_REPETITOR_BY, r);
    return r;
  }

  // TO | FOR | INDEX | ITEM | WHILE | UNTIL
  private static boolean keyword_do_repetitor_by_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_by_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, ITEM);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, UNTIL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FOR <<excluding_expr (TO | BY | INDEX | ITEM | WHILE | UNTIL)>>
  public static boolean keyword_do_repetitor_for(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_for")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FOR);
    r = r && excluding_expr(b, l + 1, keyword_do_repetitor_for_1_0_parser_);
    exit_section_(b, m, KEYWORD_DO_REPETITOR_FOR, r);
    return r;
  }

  // TO | BY | INDEX | ITEM | WHILE | UNTIL
  private static boolean keyword_do_repetitor_for_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_for_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, BY);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, ITEM);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, UNTIL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INDEX id
  public static boolean keyword_do_repetitor_index(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_index")) return false;
    if (!nextTokenIs(b, INDEX)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INDEX);
    r = r && id(b, l + 1);
    exit_section_(b, m, KEYWORD_DO_REPETITOR_INDEX, r);
    return r;
  }

  /* ********************************************************** */
  // ITEM id
  public static boolean keyword_do_repetitor_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_item")) return false;
    if (!nextTokenIs(b, ITEM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ITEM);
    r = r && id(b, l + 1);
    exit_section_(b, m, KEYWORD_DO_REPETITOR_ITEM, r);
    return r;
  }

  /* ********************************************************** */
  // TO <<excluding_expr (BY | FOR | INDEX | ITEM | WHILE | UNTIL)>>
  public static boolean keyword_do_repetitor_to(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_to")) return false;
    if (!nextTokenIs(b, TO)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TO);
    r = r && excluding_expr(b, l + 1, keyword_do_repetitor_to_1_0_parser_);
    exit_section_(b, m, KEYWORD_DO_REPETITOR_TO, r);
    return r;
  }

  // BY | FOR | INDEX | ITEM | WHILE | UNTIL
  private static boolean keyword_do_repetitor_to_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_do_repetitor_to_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BY);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, ITEM);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, UNTIL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DROP <<paren symbol_or_string>>+
  public static boolean keyword_drop(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_drop")) return false;
    if (!nextTokenIs(b, DROP)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_DROP, null);
    r = consumeToken(b, DROP);
    p = r; // pin = 1
    r = r && keyword_drop_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // <<paren symbol_or_string>>+
  private static boolean keyword_drop_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_drop_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = paren(b, l + 1, symbol_or_string_parser_);
    int c = current_position_(b);
    while (r) {
      if (!paren(b, l + 1, symbol_or_string_parser_)) break;
      if (!empty_element_parsed_guard_(b, "keyword_drop_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // EXIT expr?
  public static boolean keyword_exit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_exit")) return false;
    if (!nextTokenIs(b, EXIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_EXIT, null);
    r = consumeToken(b, EXIT);
    p = r; // pin = 1
    r = r && keyword_exit_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expr?
  private static boolean keyword_exit_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_exit_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // EXPOSE <<paren symbol_or_string>>+
  public static boolean keyword_expose(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_expose")) return false;
    if (!nextTokenIs(b, EXPOSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_EXPOSE, null);
    r = consumeToken(b, EXPOSE);
    p = r; // pin = 1
    r = r && keyword_expose_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // <<paren symbol_or_string>>+
  private static boolean keyword_expose_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_expose_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = paren(b, l + 1, symbol_or_string_parser_);
    int c = current_position_(b);
    while (r) {
      if (!paren(b, l + 1, symbol_or_string_parser_)) break;
      if (!empty_element_parsed_guard_(b, "keyword_expose_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FORWARD (keyword_forward_continue | keyword_forward_arguments | keyword_forward_array | keyword_forward_message | keyword_forward_class | keyword_forward_to)*
  public static boolean keyword_forward(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward")) return false;
    if (!nextTokenIs(b, FORWARD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_FORWARD, null);
    r = consumeToken(b, FORWARD);
    p = r; // pin = 1
    r = r && keyword_forward_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (keyword_forward_continue | keyword_forward_arguments | keyword_forward_array | keyword_forward_message | keyword_forward_class | keyword_forward_to)*
  private static boolean keyword_forward_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_forward_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_forward_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_forward_continue | keyword_forward_arguments | keyword_forward_array | keyword_forward_message | keyword_forward_class | keyword_forward_to
  private static boolean keyword_forward_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_forward_continue(b, l + 1);
    if (!r) r = keyword_forward_arguments(b, l + 1);
    if (!r) r = keyword_forward_array(b, l + 1);
    if (!r) r = keyword_forward_message(b, l + 1);
    if (!r) r = keyword_forward_class(b, l + 1);
    if (!r) r = keyword_forward_to(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ARGUMENTS <<excluding_expr (MESSAGE | CONTINUE | CLASS | TO)>>
  public static boolean keyword_forward_arguments(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_arguments")) return false;
    if (!nextTokenIs(b, ARGUMENTS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_FORWARD_ARGUMENTS, null);
    r = consumeToken(b, ARGUMENTS);
    p = r; // pin = 1
    r = r && excluding_expr(b, l + 1, keyword_forward_arguments_1_0_parser_);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // MESSAGE | CONTINUE | CLASS | TO
  private static boolean keyword_forward_arguments_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_arguments_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, TO);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ("ARRAY(" expr RPAREN) | (ARRAY LPAREN expr RPAREN)
  public static boolean keyword_forward_array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_array")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_FORWARD_ARRAY, "<keyword forward array>");
    r = keyword_forward_array_0(b, l + 1);
    if (!r) r = keyword_forward_array_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "ARRAY(" expr RPAREN
  private static boolean keyword_forward_array_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_array_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "ARRAY(");
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // ARRAY LPAREN expr RPAREN
  private static boolean keyword_forward_array_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_array_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ARRAY, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CLASS <<excluding_expr (MESSAGE | CONTINUE | ARGUMENTS | TO)>>
  public static boolean keyword_forward_class(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_class")) return false;
    if (!nextTokenIs(b, CLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_FORWARD_CLASS, null);
    r = consumeToken(b, CLASS);
    p = r; // pin = 1
    r = r && excluding_expr(b, l + 1, keyword_forward_class_1_0_parser_);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // MESSAGE | CONTINUE | ARGUMENTS | TO
  private static boolean keyword_forward_class_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_class_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, ARGUMENTS);
    if (!r) r = consumeToken(b, TO);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CONTINUE
  public static boolean keyword_forward_continue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_continue")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONTINUE);
    exit_section_(b, m, KEYWORD_FORWARD_CONTINUE, r);
    return r;
  }

  /* ********************************************************** */
  // MESSAGE <<excluding_expr (ARGUMENTS | CONTINUE | CLASS | TO)>>
  public static boolean keyword_forward_message(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_message")) return false;
    if (!nextTokenIs(b, MESSAGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_FORWARD_MESSAGE, null);
    r = consumeToken(b, MESSAGE);
    p = r; // pin = 1
    r = r && excluding_expr(b, l + 1, keyword_forward_message_1_0_parser_);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ARGUMENTS | CONTINUE | CLASS | TO
  private static boolean keyword_forward_message_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_message_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ARGUMENTS);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, TO);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (TO string_lit ) | ("TO(" <<excluding_expr (MESSAGE | CONTINUE | CLASS | ARGUMENTS)>> RPAREN) | (TO LPAREN <<excluding_expr (MESSAGE | CONTINUE | CLASS | ARGUMENTS)>> RPAREN )
  public static boolean keyword_forward_to(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_to")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_FORWARD_TO, "<keyword forward to>");
    r = keyword_forward_to_0(b, l + 1);
    if (!r) r = keyword_forward_to_1(b, l + 1);
    if (!r) r = keyword_forward_to_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // TO string_lit
  private static boolean keyword_forward_to_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_to_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TO);
    r = r && string_lit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "TO(" <<excluding_expr (MESSAGE | CONTINUE | CLASS | ARGUMENTS)>> RPAREN
  private static boolean keyword_forward_to_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_to_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "TO(");
    r = r && excluding_expr(b, l + 1, keyword_forward_to_1_1_0_parser_);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // MESSAGE | CONTINUE | CLASS | ARGUMENTS
  private static boolean keyword_forward_to_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_to_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, ARGUMENTS);
    exit_section_(b, m, null, r);
    return r;
  }

  // TO LPAREN <<excluding_expr (MESSAGE | CONTINUE | CLASS | ARGUMENTS)>> RPAREN
  private static boolean keyword_forward_to_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_to_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TO, LPAREN);
    r = r && excluding_expr(b, l + 1, keyword_forward_to_2_2_0_parser_);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // MESSAGE | CONTINUE | CLASS | ARGUMENTS
  private static boolean keyword_forward_to_2_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_forward_to_2_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, ARGUMENTS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // GUARD (ON | OFF) (WHEN expr)?
  public static boolean keyword_guard(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_guard")) return false;
    if (!nextTokenIs(b, GUARD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_GUARD, null);
    r = consumeToken(b, GUARD);
    p = r; // pin = 1
    r = r && report_error_(b, keyword_guard_1(b, l + 1));
    r = p && keyword_guard_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ON | OFF
  private static boolean keyword_guard_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_guard_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, OFF);
    exit_section_(b, m, null, r);
    return r;
  }

  // (WHEN expr)?
  private static boolean keyword_guard_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_guard_2")) return false;
    keyword_guard_2_0(b, l + 1);
    return true;
  }

  // WHEN expr
  private static boolean keyword_guard_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_guard_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IF <<excluding_expr THEN>> instruction_terminator? THEN instruction_terminator? assignments_and_keywords (instruction_terminator keyword_if_else)?
  public static boolean keyword_if(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_IF, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, excluding_expr(b, l + 1, THEN_parser_));
    r = p && report_error_(b, keyword_if_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, THEN)) && r;
    r = p && report_error_(b, keyword_if_4(b, l + 1)) && r;
    r = p && report_error_(b, assignments_and_keywords(b, l + 1)) && r;
    r = p && keyword_if_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // instruction_terminator?
  private static boolean keyword_if_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if_2")) return false;
    instruction_terminator(b, l + 1);
    return true;
  }

  // instruction_terminator?
  private static boolean keyword_if_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if_4")) return false;
    instruction_terminator(b, l + 1);
    return true;
  }

  // (instruction_terminator keyword_if_else)?
  private static boolean keyword_if_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if_6")) return false;
    keyword_if_6_0(b, l + 1);
    return true;
  }

  // instruction_terminator keyword_if_else
  private static boolean keyword_if_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instruction_terminator(b, l + 1);
    r = r && keyword_if_else(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE instruction_terminator? assignments_and_keywords
  public static boolean keyword_if_else(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if_else")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_IF_ELSE, null);
    r = consumeToken(b, ELSE);
    p = r; // pin = 1
    r = r && report_error_(b, keyword_if_else_1(b, l + 1));
    r = p && assignments_and_keywords(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // instruction_terminator?
  private static boolean keyword_if_else_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_if_else_1")) return false;
    instruction_terminator(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // keyword_address | keyword_arg | keyword_call_variants | keyword_do | keyword_drop | keyword_exit | keyword_expose | keyword_forward |  keyword_guard | keyword_if | keyword_interpret | keyword_iterate | keyword_leave
  // | keyword_nop | keyword_numeric | keyword_options | keyword_parse | keyword_procedure | keyword_pull | keyword_push | keyword_queue | keyword_raise | keyword_reply | keyword_return | keyword_say
  // | keyword_select | keyword_signal | keyword_trace | keyword_use_variants
  public static boolean keyword_instruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_instruction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_INSTRUCTION, "<keyword instruction>");
    r = keyword_address(b, l + 1);
    if (!r) r = keyword_arg(b, l + 1);
    if (!r) r = keyword_call_variants(b, l + 1);
    if (!r) r = keyword_do(b, l + 1);
    if (!r) r = keyword_drop(b, l + 1);
    if (!r) r = keyword_exit(b, l + 1);
    if (!r) r = keyword_expose(b, l + 1);
    if (!r) r = keyword_forward(b, l + 1);
    if (!r) r = keyword_guard(b, l + 1);
    if (!r) r = keyword_if(b, l + 1);
    if (!r) r = keyword_interpret(b, l + 1);
    if (!r) r = keyword_iterate(b, l + 1);
    if (!r) r = keyword_leave(b, l + 1);
    if (!r) r = keyword_nop(b, l + 1);
    if (!r) r = keyword_numeric(b, l + 1);
    if (!r) r = keyword_options(b, l + 1);
    if (!r) r = keyword_parse(b, l + 1);
    if (!r) r = keyword_procedure(b, l + 1);
    if (!r) r = keyword_pull(b, l + 1);
    if (!r) r = keyword_push(b, l + 1);
    if (!r) r = keyword_queue(b, l + 1);
    if (!r) r = keyword_raise(b, l + 1);
    if (!r) r = keyword_reply(b, l + 1);
    if (!r) r = keyword_return(b, l + 1);
    if (!r) r = keyword_say(b, l + 1);
    if (!r) r = keyword_select(b, l + 1);
    if (!r) r = keyword_signal(b, l + 1);
    if (!r) r = keyword_trace(b, l + 1);
    if (!r) r = keyword_use_variants(b, l + 1);
    exit_section_(b, l, m, r, false, keyword_instruction_recovery_parser_);
    return r;
  }

  /* ********************************************************** */
  // !instruction_terminator
  static boolean keyword_instruction_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_instruction_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !instruction_terminator(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // INTERPRET expr
  public static boolean keyword_interpret(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_interpret")) return false;
    if (!nextTokenIs(b, INTERPRET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_INTERPRET, null);
    r = consumeToken(b, INTERPRET);
    p = r; // pin = 1
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ITERATE id?
  public static boolean keyword_iterate(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_iterate")) return false;
    if (!nextTokenIs(b, ITERATE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_ITERATE, null);
    r = consumeToken(b, ITERATE);
    p = r; // pin = 1
    r = r && keyword_iterate_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // id?
  private static boolean keyword_iterate_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_iterate_1")) return false;
    id(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LEAVE id?
  public static boolean keyword_leave(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_leave")) return false;
    if (!nextTokenIs(b, LEAVE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_LEAVE, null);
    r = consumeToken(b, LEAVE);
    p = r; // pin = 1
    r = r && keyword_leave_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // id?
  private static boolean keyword_leave_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_leave_1")) return false;
    id(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (ROUTINE) | (METHOD) | (STATE) | (COMMAND) | (OPEN) | (DELETE) | (EXIST) | (CREATE) | (CASE) | (COUNT) | (SUPER) | (PACKAGE) | (ABSTRACT) | (ANNOTATE) | (ATTRIBUTE) | (CLASS) | (CONDITION) | (DIGITS) | (EXTERNAL) | (FORM) | (FUZZ) | (GET) | (GUARDED) | (INHERIT) | (LIBRARY) | (METACLASS) | (MIXINCLASS) | (NAMESPACE) | (NOPROLOG) | (PRIVATE) | (PROLOG) | (PROTECTED) | (PUBLIC) | (RESOURCE) | (RESOURCE_DATA) | (SET) | (SUBCLASS) | (TRACE) | (UNGUARDED) | (UNPROTECTED) | (ADDRESS) | (ARG) | (CALL) | (DO) | (DROP) | (EXIT) | (EXPOSE) | (FORWARD) | (GUARD) | (IF) | (INTERPRET) | (ITERATE) | (LEAVE) | (LOOP) | (NOP) | (NUMERIC) | (OPTIONS) | (PARSE) | (PROCEDURE) | (PULL) | (PUSH) | (QUEUE) | (RAISE) | (REPLY) | (RETURN) | (SAY) | (SELECT) | (SIGNAL) | (TRACE) | (USE) | (SELF) | (ADDITIONAL) | (ANY) | (ARG) | (ARGUMENTS) | (ARRAY) | (BY) | (CASELESS) | (CLASS) | (COMMANDS) | (CONTINUE) | (DESCRIPTION) | (DIGIT) | (DROP) | (ELSE) | (END) | (ENGINEERING) | (ERROR) | (EXIT) | (EXPOSE) | (FAILURE) | (FOR) | (FOREVER) | (FORM) | (FUZZ) | (HALT) | (INTERMEDIATES) | (LABEL) | (LABELS) | (LINEIN) | (LINEOUT) | (LOSTDIGITS) | (LOWER) | (MESSAGE) | (NAME) | (NOMETHOD) | (NORMAL) | (NOSTRING) | (NOTREADY) | (NOVALUE) | (OFF) | (ON) | (OTHERWISE) | (OVER) | (PROPAGATE) | (PULL) | (RESULTS) | (RETURN) | (SCIENTIFIC) | (SOURCE) | (STRICT) | (SYNTAX) | (THEN) | (TO) | (UNTIL) | (UPPER) | (USER) | (VALUE) | (VAR) | (VERSION) | (WHEN) | (WHILE) | (WITH) | (ITEM) | (INDEX)
  public static boolean keyword_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_LIST, "<keyword list>");
    r = consumeToken(b, ROUTINE);
    if (!r) r = consumeToken(b, METHOD);
    if (!r) r = consumeToken(b, STATE);
    if (!r) r = consumeToken(b, COMMAND);
    if (!r) r = consumeToken(b, OPEN);
    if (!r) r = consumeToken(b, DELETE);
    if (!r) r = consumeToken(b, EXIST);
    if (!r) r = consumeToken(b, CREATE);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, COUNT);
    if (!r) r = consumeToken(b, SUPER);
    if (!r) r = consumeToken(b, PACKAGE);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, ANNOTATE);
    if (!r) r = consumeToken(b, ATTRIBUTE);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONDITION);
    if (!r) r = consumeToken(b, DIGITS);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FORM);
    if (!r) r = consumeToken(b, FUZZ);
    if (!r) r = consumeToken(b, GET);
    if (!r) r = consumeToken(b, GUARDED);
    if (!r) r = consumeToken(b, INHERIT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, METACLASS);
    if (!r) r = consumeToken(b, MIXINCLASS);
    if (!r) r = consumeToken(b, NAMESPACE);
    if (!r) r = consumeToken(b, NOPROLOG);
    if (!r) r = consumeToken(b, PRIVATE);
    if (!r) r = consumeToken(b, PROLOG);
    if (!r) r = consumeToken(b, PROTECTED);
    if (!r) r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, RESOURCE);
    if (!r) r = consumeToken(b, RESOURCE_DATA);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, SUBCLASS);
    if (!r) r = consumeToken(b, TRACE);
    if (!r) r = consumeToken(b, UNGUARDED);
    if (!r) r = consumeToken(b, UNPROTECTED);
    if (!r) r = consumeToken(b, ADDRESS);
    if (!r) r = consumeToken(b, ARG);
    if (!r) r = consumeToken(b, CALL);
    if (!r) r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, DROP);
    if (!r) r = consumeToken(b, EXIT);
    if (!r) r = consumeToken(b, EXPOSE);
    if (!r) r = consumeToken(b, FORWARD);
    if (!r) r = consumeToken(b, GUARD);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, INTERPRET);
    if (!r) r = consumeToken(b, ITERATE);
    if (!r) r = consumeToken(b, LEAVE);
    if (!r) r = consumeToken(b, LOOP);
    if (!r) r = consumeToken(b, NOP);
    if (!r) r = consumeToken(b, NUMERIC);
    if (!r) r = consumeToken(b, OPTIONS);
    if (!r) r = consumeToken(b, PARSE);
    if (!r) r = consumeToken(b, PROCEDURE);
    if (!r) r = consumeToken(b, PULL);
    if (!r) r = consumeToken(b, PUSH);
    if (!r) r = consumeToken(b, QUEUE);
    if (!r) r = consumeToken(b, RAISE);
    if (!r) r = consumeToken(b, REPLY);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, SAY);
    if (!r) r = consumeToken(b, SELECT);
    if (!r) r = consumeToken(b, SIGNAL);
    if (!r) r = consumeToken(b, TRACE);
    if (!r) r = consumeToken(b, USE);
    if (!r) r = consumeToken(b, SELF);
    if (!r) r = consumeToken(b, ADDITIONAL);
    if (!r) r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ARG);
    if (!r) r = consumeToken(b, ARGUMENTS);
    if (!r) r = consumeToken(b, ARRAY);
    if (!r) r = consumeToken(b, BY);
    if (!r) r = consumeToken(b, CASELESS);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, COMMANDS);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, DESCRIPTION);
    if (!r) r = consumeToken(b, DIGIT);
    if (!r) r = consumeToken(b, DROP);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, END);
    if (!r) r = consumeToken(b, ENGINEERING);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, EXIT);
    if (!r) r = consumeToken(b, EXPOSE);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, FOREVER);
    if (!r) r = consumeToken(b, FORM);
    if (!r) r = consumeToken(b, FUZZ);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, INTERMEDIATES);
    if (!r) r = consumeToken(b, LABEL);
    if (!r) r = consumeToken(b, LABELS);
    if (!r) r = consumeToken(b, LINEIN);
    if (!r) r = consumeToken(b, LINEOUT);
    if (!r) r = consumeToken(b, LOSTDIGITS);
    if (!r) r = consumeToken(b, LOWER);
    if (!r) r = consumeToken(b, MESSAGE);
    if (!r) r = consumeToken(b, NAME);
    if (!r) r = consumeToken(b, NOMETHOD);
    if (!r) r = consumeToken(b, NORMAL);
    if (!r) r = consumeToken(b, NOSTRING);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = consumeToken(b, NOVALUE);
    if (!r) r = consumeToken(b, OFF);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, OTHERWISE);
    if (!r) r = consumeToken(b, OVER);
    if (!r) r = consumeToken(b, PROPAGATE);
    if (!r) r = consumeToken(b, PULL);
    if (!r) r = consumeToken(b, RESULTS);
    if (!r) r = consumeToken(b, RETURN);
    if (!r) r = consumeToken(b, SCIENTIFIC);
    if (!r) r = consumeToken(b, SOURCE);
    if (!r) r = consumeToken(b, STRICT);
    if (!r) r = consumeToken(b, SYNTAX);
    if (!r) r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, UNTIL);
    if (!r) r = consumeToken(b, UPPER);
    if (!r) r = consumeToken(b, USER);
    if (!r) r = consumeToken(b, VALUE);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, VERSION);
    if (!r) r = consumeToken(b, WHEN);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, ITEM);
    if (!r) r = consumeToken(b, INDEX);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // NOP
  public static boolean keyword_nop(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_nop")) return false;
    if (!nextTokenIs(b, NOP)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NOP);
    exit_section_(b, m, KEYWORD_NOP, r);
    return r;
  }

  /* ********************************************************** */
  // NUMERIC ((DIGITS expr?) | keyword_numeric_form | FUZZ expr?)
  public static boolean keyword_numeric(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric")) return false;
    if (!nextTokenIs(b, NUMERIC)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_NUMERIC, null);
    r = consumeToken(b, NUMERIC);
    p = r; // pin = 1
    r = r && keyword_numeric_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (DIGITS expr?) | keyword_numeric_form | FUZZ expr?
  private static boolean keyword_numeric_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_numeric_1_0(b, l + 1);
    if (!r) r = keyword_numeric_form(b, l + 1);
    if (!r) r = keyword_numeric_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DIGITS expr?
  private static boolean keyword_numeric_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIGITS);
    r = r && keyword_numeric_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expr?
  private static boolean keyword_numeric_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_1_0_1")) return false;
    expr(b, l + 1);
    return true;
  }

  // FUZZ expr?
  private static boolean keyword_numeric_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FUZZ);
    r = r && keyword_numeric_1_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // expr?
  private static boolean keyword_numeric_1_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_1_2_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FORM (SCIENTIFIC | ENGINEERING | (VALUE? expr))
  public static boolean keyword_numeric_form(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_form")) return false;
    if (!nextTokenIs(b, FORM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FORM);
    r = r && keyword_numeric_form_1(b, l + 1);
    exit_section_(b, m, KEYWORD_NUMERIC_FORM, r);
    return r;
  }

  // SCIENTIFIC | ENGINEERING | (VALUE? expr)
  private static boolean keyword_numeric_form_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_form_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SCIENTIFIC);
    if (!r) r = consumeToken(b, ENGINEERING);
    if (!r) r = keyword_numeric_form_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE? expr
  private static boolean keyword_numeric_form_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_form_1_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_numeric_form_1_2_0(b, l + 1);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE?
  private static boolean keyword_numeric_form_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_numeric_form_1_2_0")) return false;
    consumeToken(b, VALUE);
    return true;
  }

  /* ********************************************************** */
  // OPTIONS expr
  public static boolean keyword_options(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_options")) return false;
    if (!nextTokenIs(b, OPTIONS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_OPTIONS, null);
    r = consumeToken(b, OPTIONS);
    p = r; // pin = 1
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PARSE (keyword_parse_caseless | keyword_parse_upper_lower)* keyword_arg_etc (keyword_parse_caseless | keyword_parse_upper_lower)* (template_list)?
  public static boolean keyword_parse(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse")) return false;
    if (!nextTokenIs(b, PARSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_PARSE, null);
    r = consumeToken(b, PARSE);
    p = r; // pin = 1
    r = r && report_error_(b, keyword_parse_1(b, l + 1));
    r = p && report_error_(b, keyword_arg_etc(b, l + 1)) && r;
    r = p && report_error_(b, keyword_parse_3(b, l + 1)) && r;
    r = p && keyword_parse_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (keyword_parse_caseless | keyword_parse_upper_lower)*
  private static boolean keyword_parse_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_parse_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_parse_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_parse_caseless | keyword_parse_upper_lower
  private static boolean keyword_parse_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_parse_caseless(b, l + 1);
    if (!r) r = keyword_parse_upper_lower(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_parse_caseless | keyword_parse_upper_lower)*
  private static boolean keyword_parse_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_parse_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_parse_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_parse_caseless | keyword_parse_upper_lower
  private static boolean keyword_parse_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_parse_caseless(b, l + 1);
    if (!r) r = keyword_parse_upper_lower(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (template_list)?
  private static boolean keyword_parse_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_4")) return false;
    keyword_parse_4_0(b, l + 1);
    return true;
  }

  // (template_list)
  private static boolean keyword_parse_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = template_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CASELESS
  public static boolean keyword_parse_caseless(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_caseless")) return false;
    if (!nextTokenIs(b, CASELESS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CASELESS);
    exit_section_(b, m, KEYWORD_PARSE_CASELESS, r);
    return r;
  }

  /* ********************************************************** */
  // UPPER | LOWER
  public static boolean keyword_parse_upper_lower(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_parse_upper_lower")) return false;
    if (!nextTokenIs(b, "<keyword parse upper lower>", LOWER, UPPER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_PARSE_UPPER_LOWER, "<keyword parse upper lower>");
    r = consumeToken(b, UPPER);
    if (!r) r = consumeToken(b, LOWER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PROCEDURE (keyword_expose)?
  public static boolean keyword_procedure(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_procedure")) return false;
    if (!nextTokenIs(b, PROCEDURE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_PROCEDURE, null);
    r = consumeToken(b, PROCEDURE);
    p = r; // pin = 1
    r = r && keyword_procedure_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (keyword_expose)?
  private static boolean keyword_procedure_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_procedure_1")) return false;
    keyword_procedure_1_0(b, l + 1);
    return true;
  }

  // (keyword_expose)
  private static boolean keyword_procedure_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_procedure_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_expose(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PULL template_list*
  public static boolean keyword_pull(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_pull")) return false;
    if (!nextTokenIs(b, PULL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_PULL, null);
    r = consumeToken(b, PULL);
    p = r; // pin = 1
    r = r && keyword_pull_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // template_list*
  private static boolean keyword_pull_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_pull_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!template_list(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_pull_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // PUSH expr?
  public static boolean keyword_push(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_push")) return false;
    if (!nextTokenIs(b, PUSH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_PUSH, null);
    r = consumeToken(b, PUSH);
    p = r; // pin = 1
    r = r && keyword_push_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expr?
  private static boolean keyword_push_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_push_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // QUEUE expr?
  public static boolean keyword_queue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_queue")) return false;
    if (!nextTokenIs(b, QUEUE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_QUEUE, null);
    r = consumeToken(b, QUEUE);
    p = r; // pin = 1
    r = r && keyword_queue_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expr?
  private static boolean keyword_queue_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_queue_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RAISE (keyword_raise_error_etc | keyword_raise_options)*
  public static boolean keyword_raise(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise")) return false;
    if (!nextTokenIs(b, RAISE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RAISE, null);
    r = consumeToken(b, RAISE);
    p = r; // pin = 1
    r = r && keyword_raise_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (keyword_raise_error_etc | keyword_raise_options)*
  private static boolean keyword_raise_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_raise_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_raise_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_raise_error_etc | keyword_raise_options
  private static boolean keyword_raise_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_error_etc(b, l + 1);
    if (!r) r = keyword_raise_options(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (ERROR (lit | (LPAREN expr RPAREN))) | (FAILURE (lit | (LPAREN expr RPAREN))) | HALT | LOSTDIGITS | NOMETHOD | NOSTRING | NOTREADY | NOVALUE | (SYNTAX (lit | (LPAREN expr RPAREN))) | (USER symbol_or_string) | PROPAGATE
  public static boolean keyword_raise_error_etc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RAISE_ERROR_ETC, "<keyword raise error etc>");
    r = keyword_raise_error_etc_0(b, l + 1);
    if (!r) r = keyword_raise_error_etc_1(b, l + 1);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, LOSTDIGITS);
    if (!r) r = consumeToken(b, NOMETHOD);
    if (!r) r = consumeToken(b, NOSTRING);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = consumeToken(b, NOVALUE);
    if (!r) r = keyword_raise_error_etc_8(b, l + 1);
    if (!r) r = keyword_raise_error_etc_9(b, l + 1);
    if (!r) r = consumeToken(b, PROPAGATE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ERROR (lit | (LPAREN expr RPAREN))
  private static boolean keyword_raise_error_etc_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ERROR);
    r = r && keyword_raise_error_etc_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // lit | (LPAREN expr RPAREN)
  private static boolean keyword_raise_error_etc_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lit(b, l + 1);
    if (!r) r = keyword_raise_error_etc_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expr RPAREN
  private static boolean keyword_raise_error_etc_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_0_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // FAILURE (lit | (LPAREN expr RPAREN))
  private static boolean keyword_raise_error_etc_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FAILURE);
    r = r && keyword_raise_error_etc_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // lit | (LPAREN expr RPAREN)
  private static boolean keyword_raise_error_etc_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lit(b, l + 1);
    if (!r) r = keyword_raise_error_etc_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expr RPAREN
  private static boolean keyword_raise_error_etc_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_1_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // SYNTAX (lit | (LPAREN expr RPAREN))
  private static boolean keyword_raise_error_etc_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_8")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SYNTAX);
    r = r && keyword_raise_error_etc_8_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // lit | (LPAREN expr RPAREN)
  private static boolean keyword_raise_error_etc_8_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_8_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lit(b, l + 1);
    if (!r) r = keyword_raise_error_etc_8_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN expr RPAREN
  private static boolean keyword_raise_error_etc_8_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_8_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // USER symbol_or_string
  private static boolean keyword_raise_error_etc_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, USER);
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ERROR | FAILURE | HALT | LOSTDIGITS | NOMETHOD | NOSTRING | NOTREADY | NOVALUE | SYNTAX | USER | PROPAGATE
  public static boolean keyword_raise_error_etc_keywords(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_error_etc_keywords")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RAISE_ERROR_ETC_KEYWORDS, "<keyword raise error etc keywords>");
    r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, LOSTDIGITS);
    if (!r) r = consumeToken(b, NOMETHOD);
    if (!r) r = consumeToken(b, NOSTRING);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = consumeToken(b, NOVALUE);
    if (!r) r = consumeToken(b, SYNTAX);
    if (!r) r = consumeToken(b, USER);
    if (!r) r = consumeToken(b, PROPAGATE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (keyword_raise_options_array | keyword_raise_options_description | keyword_raise_options_exit_return | keyword_raise_options_additional)+
  public static boolean keyword_raise_options(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RAISE_OPTIONS, "<keyword raise options>");
    r = keyword_raise_options_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!keyword_raise_options_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_raise_options", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // keyword_raise_options_array | keyword_raise_options_description | keyword_raise_options_exit_return | keyword_raise_options_additional
  private static boolean keyword_raise_options_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_options_array(b, l + 1);
    if (!r) r = keyword_raise_options_description(b, l + 1);
    if (!r) r = keyword_raise_options_exit_return(b, l + 1);
    if (!r) r = keyword_raise_options_additional(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ADDITIONAL <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | DESCRIPTION | EXIT | RETURN)>>
  public static boolean keyword_raise_options_additional(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_additional")) return false;
    if (!nextTokenIs(b, ADDITIONAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ADDITIONAL);
    r = r && excluding_expr(b, l + 1, keyword_raise_options_additional_1_0_parser_);
    exit_section_(b, m, KEYWORD_RAISE_OPTIONS_ADDITIONAL, r);
    return r;
  }

  // keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | DESCRIPTION | EXIT | RETURN
  private static boolean keyword_raise_options_additional_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_additional_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_error_etc_keywords(b, l + 1);
    if (!r) r = consumeToken(b, "ARRAY(");
    if (!r) r = consumeToken(b, ARRAY);
    if (!r) r = consumeToken(b, DESCRIPTION);
    if (!r) r = consumeToken(b, EXIT);
    if (!r) r = consumeToken(b, RETURN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ( ("ARRAY(" (expr| COMMA)*) | (ARRAY "(" (expr| COMMA)* )) RPAREN
  public static boolean keyword_raise_options_array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RAISE_OPTIONS_ARRAY, "<keyword raise options array>");
    r = keyword_raise_options_array_0(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ("ARRAY(" (expr| COMMA)*) | (ARRAY "(" (expr| COMMA)* )
  private static boolean keyword_raise_options_array_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_options_array_0_0(b, l + 1);
    if (!r) r = keyword_raise_options_array_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "ARRAY(" (expr| COMMA)*
  private static boolean keyword_raise_options_array_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "ARRAY(");
    r = r && keyword_raise_options_array_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (expr| COMMA)*
  private static boolean keyword_raise_options_array_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_raise_options_array_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_raise_options_array_0_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // expr| COMMA
  private static boolean keyword_raise_options_array_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  // ARRAY "(" (expr| COMMA)*
  private static boolean keyword_raise_options_array_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ARRAY, LPAREN);
    r = r && keyword_raise_options_array_0_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (expr| COMMA)*
  private static boolean keyword_raise_options_array_0_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0_1_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_raise_options_array_0_1_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_raise_options_array_0_1_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // expr| COMMA
  private static boolean keyword_raise_options_array_0_1_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_array_0_1_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DESCRIPTION <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | EXIT | RETURN)>>
  public static boolean keyword_raise_options_description(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_description")) return false;
    if (!nextTokenIs(b, DESCRIPTION)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DESCRIPTION);
    r = r && excluding_expr(b, l + 1, keyword_raise_options_description_1_0_parser_);
    exit_section_(b, m, KEYWORD_RAISE_OPTIONS_DESCRIPTION, r);
    return r;
  }

  // keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | EXIT | RETURN
  private static boolean keyword_raise_options_description_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_description_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_error_etc_keywords(b, l + 1);
    if (!r) r = consumeToken(b, "ARRAY(");
    if (!r) r = consumeToken(b, ARRAY);
    if (!r) r = consumeToken(b, ADDITIONAL);
    if (!r) r = consumeToken(b, EXIT);
    if (!r) r = consumeToken(b, RETURN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (EXIT <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION)>>?) | (RETURN <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION)>>?)
  public static boolean keyword_raise_options_exit_return(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return")) return false;
    if (!nextTokenIs(b, "<keyword raise options exit return>", EXIT, RETURN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RAISE_OPTIONS_EXIT_RETURN, "<keyword raise options exit return>");
    r = keyword_raise_options_exit_return_0(b, l + 1);
    if (!r) r = keyword_raise_options_exit_return_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EXIT <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION)>>?
  private static boolean keyword_raise_options_exit_return_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXIT);
    r = r && keyword_raise_options_exit_return_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION)>>?
  private static boolean keyword_raise_options_exit_return_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return_0_1")) return false;
    excluding_expr(b, l + 1, keyword_raise_options_exit_return_0_1_0_0_parser_);
    return true;
  }

  // keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION
  private static boolean keyword_raise_options_exit_return_0_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return_0_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_error_etc_keywords(b, l + 1);
    if (!r) r = consumeToken(b, "ARRAY(");
    if (!r) r = consumeToken(b, ARRAY);
    if (!r) r = consumeToken(b, ADDITIONAL);
    if (!r) r = consumeToken(b, DESCRIPTION);
    exit_section_(b, m, null, r);
    return r;
  }

  // RETURN <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION)>>?
  private static boolean keyword_raise_options_exit_return_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RETURN);
    r = r && keyword_raise_options_exit_return_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<excluding_expr (keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION)>>?
  private static boolean keyword_raise_options_exit_return_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return_1_1")) return false;
    excluding_expr(b, l + 1, keyword_raise_options_exit_return_1_1_0_0_parser_);
    return true;
  }

  // keyword_raise_error_etc_keywords | "ARRAY(" | ARRAY | ADDITIONAL | DESCRIPTION
  private static boolean keyword_raise_options_exit_return_1_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_raise_options_exit_return_1_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_raise_error_etc_keywords(b, l + 1);
    if (!r) r = consumeToken(b, "ARRAY(");
    if (!r) r = consumeToken(b, ARRAY);
    if (!r) r = consumeToken(b, ADDITIONAL);
    if (!r) r = consumeToken(b, DESCRIPTION);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // REPLY expr?
  public static boolean keyword_reply(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_reply")) return false;
    if (!nextTokenIs(b, REPLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_REPLY, null);
    r = consumeToken(b, REPLY);
    p = r; // pin = 1
    r = r && keyword_reply_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expr?
  private static boolean keyword_reply_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_reply_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // RETURN expr?
  public static boolean keyword_return(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_return")) return false;
    if (!nextTokenIs(b, RETURN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_RETURN, null);
    r = consumeToken(b, RETURN);
    p = r; // pin = 1
    r = r && keyword_return_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expr?
  private static boolean keyword_return_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_return_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SAY expr?
  public static boolean keyword_say(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_say")) return false;
    if (!nextTokenIs(b, SAY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_SAY, null);
    r = consumeToken(b, SAY);
    p = r; // pin = 1
    r = r && keyword_say_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // expr?
  private static boolean keyword_say_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_say_1")) return false;
    expr(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SELECT keyword_select_1 instruction_terminator (keyword_select_when )+ keyword_select_otherwise?  END symbol_or_string?
  public static boolean keyword_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select")) return false;
    if (!nextTokenIs(b, SELECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_SELECT, null);
    r = consumeToken(b, SELECT);
    p = r; // pin = 1
    r = r && report_error_(b, keyword_select_1(b, l + 1));
    r = p && report_error_(b, instruction_terminator(b, l + 1)) && r;
    r = p && report_error_(b, keyword_select_3(b, l + 1)) && r;
    r = p && report_error_(b, keyword_select_4(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, END)) && r;
    r = p && keyword_select_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (keyword_select_when )+
  private static boolean keyword_select_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_select_3_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!keyword_select_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_select_3", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_select_when )
  private static boolean keyword_select_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_select_when(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyword_select_otherwise?
  private static boolean keyword_select_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_4")) return false;
    keyword_select_otherwise(b, l + 1);
    return true;
  }

  // symbol_or_string?
  private static boolean keyword_select_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_6")) return false;
    symbol_or_string(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (keyword_select_case | keyword_select_label)*
  static boolean keyword_select_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_select_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_select_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_select_case | keyword_select_label
  private static boolean keyword_select_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_select_case(b, l + 1);
    if (!r) r = keyword_select_label(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CASE <<excluding_expr (WHEN | OTHERWISE | LABEL)>>
  public static boolean keyword_select_case(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_case")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CASE);
    r = r && excluding_expr(b, l + 1, keyword_select_case_1_0_parser_);
    exit_section_(b, m, KEYWORD_SELECT_CASE, r);
    return r;
  }

  // WHEN | OTHERWISE | LABEL
  private static boolean keyword_select_case_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_case_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    if (!r) r = consumeToken(b, OTHERWISE);
    if (!r) r = consumeToken(b, LABEL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LABEL symbol_or_string
  public static boolean keyword_select_label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_label")) return false;
    if (!nextTokenIs(b, LABEL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LABEL);
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, m, KEYWORD_SELECT_LABEL, r);
    return r;
  }

  /* ********************************************************** */
  // OTHERWISE instruction_terminator? instruction*
  public static boolean keyword_select_otherwise(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_otherwise")) return false;
    if (!nextTokenIs(b, OTHERWISE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OTHERWISE);
    r = r && keyword_select_otherwise_1(b, l + 1);
    r = r && keyword_select_otherwise_2(b, l + 1);
    exit_section_(b, m, KEYWORD_SELECT_OTHERWISE, r);
    return r;
  }

  // instruction_terminator?
  private static boolean keyword_select_otherwise_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_otherwise_1")) return false;
    instruction_terminator(b, l + 1);
    return true;
  }

  // instruction*
  private static boolean keyword_select_otherwise_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_otherwise_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!instruction(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_select_otherwise_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // WHEN <<excluding_expr THEN>> instruction_terminator? THEN instruction_terminator? instruction
  public static boolean keyword_select_when(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_when")) return false;
    if (!nextTokenIs(b, WHEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WHEN);
    r = r && excluding_expr(b, l + 1, THEN_parser_);
    r = r && keyword_select_when_2(b, l + 1);
    r = r && consumeToken(b, THEN);
    r = r && keyword_select_when_4(b, l + 1);
    r = r && instruction(b, l + 1);
    exit_section_(b, m, KEYWORD_SELECT_WHEN, r);
    return r;
  }

  // instruction_terminator?
  private static boolean keyword_select_when_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_when_2")) return false;
    instruction_terminator(b, l + 1);
    return true;
  }

  // instruction_terminator?
  private static boolean keyword_select_when_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_select_when_4")) return false;
    instruction_terminator(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SIGNAL (keyword_signal_on | keyword_signal_off | keyword_signal_val)
  public static boolean keyword_signal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal")) return false;
    if (!nextTokenIs(b, SIGNAL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_SIGNAL, null);
    r = consumeToken(b, SIGNAL);
    p = r; // pin = 1
    r = r && keyword_signal_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // keyword_signal_on | keyword_signal_off | keyword_signal_val
  private static boolean keyword_signal_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_signal_on(b, l + 1);
    if (!r) r = keyword_signal_off(b, l + 1);
    if (!r) r = keyword_signal_val(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ANY | ERROR | FAILURE | HALT | LOSTDIGITS | NOMETHOD | NOSTRING | NOTREADY | NOVALUE | SYNTAX | (USER symbol_or_string)
  public static boolean keyword_signal_any_etc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_any_etc")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_SIGNAL_ANY_ETC, "<keyword signal any etc>");
    r = consumeToken(b, ANY);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, HALT);
    if (!r) r = consumeToken(b, LOSTDIGITS);
    if (!r) r = consumeToken(b, NOMETHOD);
    if (!r) r = consumeToken(b, NOSTRING);
    if (!r) r = consumeToken(b, NOTREADY);
    if (!r) r = consumeToken(b, NOVALUE);
    if (!r) r = consumeToken(b, SYNTAX);
    if (!r) r = keyword_signal_any_etc_10(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // USER symbol_or_string
  private static boolean keyword_signal_any_etc_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_any_etc_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, USER);
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OFF keyword_signal_any_etc
  public static boolean keyword_signal_off(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_off")) return false;
    if (!nextTokenIs(b, OFF)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OFF);
    r = r && keyword_signal_any_etc(b, l + 1);
    exit_section_(b, m, KEYWORD_SIGNAL_OFF, r);
    return r;
  }

  /* ********************************************************** */
  // ON (keyword_signal_any_etc | keyword_signal_on_name)*
  public static boolean keyword_signal_on(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_on")) return false;
    if (!nextTokenIs(b, ON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ON);
    r = r && keyword_signal_on_1(b, l + 1);
    exit_section_(b, m, KEYWORD_SIGNAL_ON, r);
    return r;
  }

  // (keyword_signal_any_etc | keyword_signal_on_name)*
  private static boolean keyword_signal_on_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_on_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_signal_on_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_signal_on_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // keyword_signal_any_etc | keyword_signal_on_name
  private static boolean keyword_signal_on_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_on_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_signal_any_etc(b, l + 1);
    if (!r) r = keyword_signal_on_name(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NAME symbol_or_string
  public static boolean keyword_signal_on_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_on_name")) return false;
    if (!nextTokenIs(b, NAME)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NAME);
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, m, KEYWORD_SIGNAL_ON_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // (VALUE? expr)| symbol_or_string
  public static boolean keyword_signal_val(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_val")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_SIGNAL_VAL, "<keyword signal val>");
    r = keyword_signal_val_0(b, l + 1);
    if (!r) r = symbol_or_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // VALUE? expr
  private static boolean keyword_signal_val_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_val_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_signal_val_0_0(b, l + 1);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE?
  private static boolean keyword_signal_val_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_signal_val_0_0")) return false;
    consumeToken(b, VALUE);
    return true;
  }

  /* ********************************************************** */
  // TRACE (number | keyword_trace_keyword_part | symbol_or_string | (VALUE? expr))
  public static boolean keyword_trace(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace")) return false;
    if (!nextTokenIs(b, TRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_TRACE, null);
    r = consumeToken(b, TRACE);
    p = r; // pin = 1
    r = r && keyword_trace_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // number | keyword_trace_keyword_part | symbol_or_string | (VALUE? expr)
  private static boolean keyword_trace_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER);
    if (!r) r = keyword_trace_keyword_part(b, l + 1);
    if (!r) r = symbol_or_string(b, l + 1);
    if (!r) r = keyword_trace_1_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE? expr
  private static boolean keyword_trace_1_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace_1_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_trace_1_3_0(b, l + 1);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE?
  private static boolean keyword_trace_1_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace_1_3_0")) return false;
    consumeToken(b, VALUE);
    return true;
  }

  /* ********************************************************** */
  // "?"* (NORMAL|ALL|COMMANDS|ERROR|FAILURE|INTERMEDIATES|LABELS|OFF|RESULTS)
  public static boolean keyword_trace_keyword_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace_keyword_part")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_TRACE_KEYWORD_PART, "<keyword trace keyword part>");
    r = keyword_trace_keyword_part_0(b, l + 1);
    r = r && keyword_trace_keyword_part_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "?"*
  private static boolean keyword_trace_keyword_part_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace_keyword_part_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, "?")) break;
      if (!empty_element_parsed_guard_(b, "keyword_trace_keyword_part_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // NORMAL|ALL|COMMANDS|ERROR|FAILURE|INTERMEDIATES|LABELS|OFF|RESULTS
  private static boolean keyword_trace_keyword_part_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_trace_keyword_part_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NORMAL);
    if (!r) r = consumeToken(b, ALL);
    if (!r) r = consumeToken(b, COMMANDS);
    if (!r) r = consumeToken(b, ERROR);
    if (!r) r = consumeToken(b, FAILURE);
    if (!r) r = consumeToken(b, INTERMEDIATES);
    if (!r) r = consumeToken(b, LABELS);
    if (!r) r = consumeToken(b, OFF);
    if (!r) r = consumeToken(b, RESULTS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRICT? ARG (COMMA | (assignment | expr) )*
  public static boolean keyword_use(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use")) return false;
    if (!nextTokenIs(b, "<keyword use>", ARG, STRICT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_USE, "<keyword use>");
    r = keyword_use_0(b, l + 1);
    r = r && consumeToken(b, ARG);
    r = r && keyword_use_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // STRICT?
  private static boolean keyword_use_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_0")) return false;
    consumeToken(b, STRICT);
    return true;
  }

  // (COMMA | (assignment | expr) )*
  private static boolean keyword_use_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!keyword_use_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_use_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA | (assignment | expr)
  private static boolean keyword_use_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = keyword_use_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // assignment | expr
  private static boolean keyword_use_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_2_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignment(b, l + 1);
    if (!r) r = expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LOCAL id*
  public static boolean keyword_use_local(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_local")) return false;
    if (!nextTokenIs(b, LOCAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LOCAL);
    r = r && keyword_use_local_1(b, l + 1);
    exit_section_(b, m, KEYWORD_USE_LOCAL, r);
    return r;
  }

  // id*
  private static boolean keyword_use_local_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_local_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!id(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "keyword_use_local_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // USE (keyword_use | keyword_use_local)
  public static boolean keyword_use_variants(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_variants")) return false;
    if (!nextTokenIs(b, USE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_USE_VARIANTS, null);
    r = consumeToken(b, USE);
    p = r; // pin = 1
    r = r && keyword_use_variants_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // keyword_use | keyword_use_local
  private static boolean keyword_use_variants_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyword_use_variants_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_use(b, l + 1);
    if (!r) r = keyword_use_local(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (symbol_or_string | keyword_list) COLON (keyword_procedure keyword_expose?)? (instruction_terminator | instruction)
  public static boolean label_instruction(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_instruction")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LABEL_INSTRUCTION, "<label instruction>");
    r = label_instruction_0(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && label_instruction_2(b, l + 1);
    r = r && label_instruction_3(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // symbol_or_string | keyword_list
  private static boolean label_instruction_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_instruction_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_or_string(b, l + 1);
    if (!r) r = keyword_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (keyword_procedure keyword_expose?)?
  private static boolean label_instruction_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_instruction_2")) return false;
    label_instruction_2_0(b, l + 1);
    return true;
  }

  // keyword_procedure keyword_expose?
  private static boolean label_instruction_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_instruction_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_procedure(b, l + 1);
    r = r && label_instruction_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // keyword_expose?
  private static boolean label_instruction_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_instruction_2_0_1")) return false;
    keyword_expose(b, l + 1);
    return true;
  }

  // instruction_terminator | instruction
  private static boolean label_instruction_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_instruction_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = instruction_terminator(b, l + 1);
    if (!r) r = instruction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // string_lit | number_lit
  static boolean lit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lit")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = string_lit(b, l + 1);
    if (!r) r = number_lit(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // string_lit | negative_numeric_expr | numeric_expr | bool_lit |  NIL
  public static boolean literal_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPR, "<literal expr>");
    r = string_lit(b, l + 1);
    if (!r) r = negative_numeric_expr(b, l + 1);
    if (!r) r = numeric_expr(b, l + 1);
    if (!r) r = bool_lit(b, l + 1);
    if (!r) r = consumeToken(b, NIL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // message_term_receiver message_term_tail+
  public static boolean message_term(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MESSAGE_TERM, "<message term>");
    r = message_term_receiver(b, l + 1);
    r = r && message_term_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // message_term_tail+
  private static boolean message_term_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term_tail(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!message_term_tail(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "message_term_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACK (expr | COMMA)* RBRACK
  public static boolean message_term_brack(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_brack")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACK);
    r = r && message_term_brack_1(b, l + 1);
    r = r && consumeToken(b, RBRACK);
    exit_section_(b, m, MESSAGE_TERM_BRACK, r);
    return r;
  }

  // (expr | COMMA)*
  private static boolean message_term_brack_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_brack_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!message_term_brack_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "message_term_brack_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // expr | COMMA
  private static boolean message_term_brack_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_brack_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (TILDE TILDE) | TILDE
  public static boolean message_term_operator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_operator")) return false;
    if (!nextTokenIs(b, TILDE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term_operator_0(b, l + 1);
    if (!r) r = consumeToken(b, TILDE);
    exit_section_(b, m, MESSAGE_TERM_OPERATOR, r);
    return r;
  }

  // TILDE TILDE
  private static boolean message_term_operator_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_operator_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, TILDE, TILDE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // keyword_list | array_term_parenthized | lit | id | function_call
  public static boolean message_term_receiver(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_receiver")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MESSAGE_TERM_RECEIVER, "<message term receiver>");
    r = keyword_list(b, l + 1);
    if (!r) r = array_term_parenthized(b, l + 1);
    if (!r) r = lit(b, l + 1);
    if (!r) r = id(b, l + 1);
    if (!r) r = function_call(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // message_term_tilde | message_term_brack
  public static boolean message_term_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tail")) return false;
    if (!nextTokenIs(b, "<message term tail>", LBRACK, TILDE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MESSAGE_TERM_TAIL, "<message term tail>");
    r = message_term_tilde(b, l + 1);
    if (!r) r = message_term_brack(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // message_term_operator
  //                                (
  //                                    ((symbol_or_string COLON)? function_call_without_namespace) | ((symbol_or_string) (COLON symbol_or_string)?)
  //                                )
  public static boolean message_term_tilde(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde")) return false;
    if (!nextTokenIs(b, TILDE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term_operator(b, l + 1);
    r = r && message_term_tilde_1(b, l + 1);
    exit_section_(b, m, MESSAGE_TERM_TILDE, r);
    return r;
  }

  // ((symbol_or_string COLON)? function_call_without_namespace) | ((symbol_or_string) (COLON symbol_or_string)?)
  private static boolean message_term_tilde_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term_tilde_1_0(b, l + 1);
    if (!r) r = message_term_tilde_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (symbol_or_string COLON)? function_call_without_namespace
  private static boolean message_term_tilde_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term_tilde_1_0_0(b, l + 1);
    r = r && function_call_without_namespace(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (symbol_or_string COLON)?
  private static boolean message_term_tilde_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_0_0")) return false;
    message_term_tilde_1_0_0_0(b, l + 1);
    return true;
  }

  // symbol_or_string COLON
  private static boolean message_term_tilde_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_or_string(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  // (symbol_or_string) (COLON symbol_or_string)?
  private static boolean message_term_tilde_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term_tilde_1_1_0(b, l + 1);
    r = r && message_term_tilde_1_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (symbol_or_string)
  private static boolean message_term_tilde_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_or_string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COLON symbol_or_string)?
  private static boolean message_term_tilde_1_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_1_1")) return false;
    message_term_tilde_1_1_1_0(b, l + 1);
    return true;
  }

  // COLON symbol_or_string
  private static boolean message_term_tilde_1_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "message_term_tilde_1_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && symbol_or_string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // COMMA? (<<expr_raw <<p>>>> ((COMMA |(PIPE PIPE))? <<meta_expr <<p>>>>)*)
  static boolean meta_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "meta_expr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = meta_expr_0(b, l + 1);
    r = r && meta_expr_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMA?
  private static boolean meta_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "meta_expr_0")) return false;
    consumeToken(b, COMMA);
    return true;
  }

  // <<expr_raw <<p>>>> ((COMMA |(PIPE PIPE))? <<meta_expr <<p>>>>)*
  private static boolean meta_expr_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "meta_expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr_raw(b, l + 1, _p);
    r = r && meta_expr_1_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // ((COMMA |(PIPE PIPE))? <<meta_expr <<p>>>>)*
  private static boolean meta_expr_1_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "meta_expr_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!meta_expr_1_1_0(b, l + 1, _p)) break;
      if (!empty_element_parsed_guard_(b, "meta_expr_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (COMMA |(PIPE PIPE))? <<meta_expr <<p>>>>
  private static boolean meta_expr_1_1_0(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "meta_expr_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = meta_expr_1_1_0_0(b, l + 1);
    r = r && meta_expr(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA |(PIPE PIPE))?
  private static boolean meta_expr_1_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "meta_expr_1_1_0_0")) return false;
    meta_expr_1_1_0_0_0(b, l + 1);
    return true;
  }

  // COMMA |(PIPE PIPE)
  private static boolean meta_expr_1_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "meta_expr_1_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = meta_expr_1_1_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // PIPE PIPE
  private static boolean meta_expr_1_1_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "meta_expr_1_1_0_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PIPE, PIPE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // mul_op NEGATION? <<primary <<p>>>>
  public static boolean mul_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "mul_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, MUL_EXPR, null);
    r = mul_op(b, l + 1);
    r = r && mul_expr_1(b, l + 1);
    r = r && primary(b, l + 1, _p);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NEGATION?
  private static boolean mul_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_expr_1")) return false;
    consumeToken(b, NEGATION);
    return true;
  }

  /* ********************************************************** */
  // (MUL MUL)|MUL|(DIV DIV)| DIV|INTDIV
  static boolean mul_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_op")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = mul_op_0(b, l + 1);
    if (!r) r = consumeToken(b, MUL);
    if (!r) r = mul_op_2(b, l + 1);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, INTDIV);
    exit_section_(b, m, null, r);
    return r;
  }

  // MUL MUL
  private static boolean mul_op_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_op_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, MUL, MUL);
    exit_section_(b, m, null, r);
    return r;
  }

  // DIV DIV
  private static boolean mul_op_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_op_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, DIV, DIV);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (identifier | keyword_list) COLON
  public static boolean namespace_prefix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespace_prefix")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMESPACE_PREFIX, "<namespace prefix>");
    r = namespace_prefix_0(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // identifier | keyword_list
  private static boolean namespace_prefix_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespace_prefix_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = keyword_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MINUS (INTEGER_LITERAL | FLOAT_LITERAL)
  public static boolean negative_numeric_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "negative_numeric_expr")) return false;
    if (!nextTokenIs(b, MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MINUS);
    r = r && negative_numeric_expr_1(b, l + 1);
    exit_section_(b, m, NEGATIVE_NUMERIC_EXPR, r);
    return r;
  }

  // INTEGER_LITERAL | FLOAT_LITERAL
  private static boolean negative_numeric_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "negative_numeric_expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    if (!r) r = consumeToken(b, FLOAT_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // EOL
  public static boolean new_line(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "new_line")) return false;
    if (!nextTokenIs(b, EOL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EOL);
    exit_section_(b, m, NEW_LINE, r);
    return r;
  }

  /* ********************************************************** */
  // !(END (id)?)
  static boolean not_end(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_end")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_end_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // END (id)?
  private static boolean not_end_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_end_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END);
    r = r && not_end_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (id)?
  private static boolean not_end_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_end_0_1")) return false;
    not_end_0_1_0(b, l + 1);
    return true;
  }

  // (id)
  private static boolean not_end_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_end_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = id(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (MINUS? (INTEGER_LITERAL | FLOAT_LITERAL)) | bool_lit
  public static boolean number_lit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number_lit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NUMBER_LIT, "<number lit>");
    r = number_lit_0(b, l + 1);
    if (!r) r = bool_lit(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // MINUS? (INTEGER_LITERAL | FLOAT_LITERAL)
  private static boolean number_lit_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number_lit_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = number_lit_0_0(b, l + 1);
    r = r && number_lit_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // MINUS?
  private static boolean number_lit_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number_lit_0_0")) return false;
    consumeToken(b, MINUS);
    return true;
  }

  // INTEGER_LITERAL | FLOAT_LITERAL
  private static boolean number_lit_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number_lit_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    if (!r) r = consumeToken(b, FLOAT_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PLUS? (INTEGER_LITERAL | FLOAT_LITERAL)
  public static boolean numeric_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numeric_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NUMERIC_EXPR, "<numeric expr>");
    r = numeric_expr_0(b, l + 1);
    r = r && numeric_expr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // PLUS?
  private static boolean numeric_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numeric_expr_0")) return false;
    consumeToken(b, PLUS);
    return true;
  }

  // INTEGER_LITERAL | FLOAT_LITERAL
  private static boolean numeric_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numeric_expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTEGER_LITERAL);
    if (!r) r = consumeToken(b, FLOAT_LITERAL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // shebang? (new_line | instruction)* (directive)*
  static boolean package_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "package_$")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = package_0(b, l + 1);
    r = r && package_1(b, l + 1);
    r = r && package_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // shebang?
  private static boolean package_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "package_0")) return false;
    shebang(b, l + 1);
    return true;
  }

  // (new_line | instruction)*
  private static boolean package_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "package_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!package_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "package_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // new_line | instruction
  private static boolean package_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "package_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = new_line(b, l + 1);
    if (!r) r = instruction(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (directive)*
  private static boolean package_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "package_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!package_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "package_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // (directive)
  private static boolean package_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "package_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = directive(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // DOT | <<parameter_meta_expr DUMMYOBJECT>>
  public static boolean parameter_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_EXPR, "<parameter expr>");
    r = consumeToken(b, DOT);
    if (!r) r = parameter_meta_expr(b, l + 1, DUMMYOBJECT_parser_);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !COMMA (<<expr_raw <<p>>>> (((PIPE PIPE))? !COMMA <<meta_expr <<p>>>>)*)
  static boolean parameter_meta_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "parameter_meta_expr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_meta_expr_0(b, l + 1);
    r = r && parameter_meta_expr_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // !COMMA
  private static boolean parameter_meta_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, COMMA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<expr_raw <<p>>>> (((PIPE PIPE))? !COMMA <<meta_expr <<p>>>>)*
  private static boolean parameter_meta_expr_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr_raw(b, l + 1, _p);
    r = r && parameter_meta_expr_1_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // (((PIPE PIPE))? !COMMA <<meta_expr <<p>>>>)*
  private static boolean parameter_meta_expr_1_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_1_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!parameter_meta_expr_1_1_0(b, l + 1, _p)) break;
      if (!empty_element_parsed_guard_(b, "parameter_meta_expr_1_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // ((PIPE PIPE))? !COMMA <<meta_expr <<p>>>>
  private static boolean parameter_meta_expr_1_1_0(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_meta_expr_1_1_0_0(b, l + 1);
    r = r && parameter_meta_expr_1_1_0_1(b, l + 1);
    r = r && meta_expr(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // ((PIPE PIPE))?
  private static boolean parameter_meta_expr_1_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_1_1_0_0")) return false;
    parameter_meta_expr_1_1_0_0_0(b, l + 1);
    return true;
  }

  // PIPE PIPE
  private static boolean parameter_meta_expr_1_1_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_1_1_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, PIPE, PIPE);
    exit_section_(b, m, null, r);
    return r;
  }

  // !COMMA
  private static boolean parameter_meta_expr_1_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_meta_expr_1_1_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, COMMA);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<p>> | (LPAREN (<<paren  <<p>>>>| <<p>>) RPAREN)
  static boolean paren(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "paren")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = _p.parse(b, l);
    if (!r) r = paren_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // LPAREN (<<paren  <<p>>>>| <<p>>) RPAREN
  private static boolean paren_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "paren_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && paren_1_1(b, l + 1, _p);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<paren  <<p>>>>| <<p>>
  private static boolean paren_1_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "paren_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = paren(b, l + 1, _p);
    if (!r) r = _p.parse(b, l);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // MINUS? '(' expr ')'
  public static boolean paren_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expr")) return false;
    if (!nextTokenIs(b, "", LPAREN, MINUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PAREN_EXPR, null);
    r = paren_expr_0(b, l + 1);
    r = r && consumeToken(b, LPAREN);
    p = r; // pin = 2
    r = r && report_error_(b, expr(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // MINUS?
  private static boolean paren_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paren_expr_0")) return false;
    consumeToken(b, MINUS);
    return true;
  }

  /* ********************************************************** */
  // plus_op <<factor <<p>>>>
  public static boolean plus_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "plus_expr")) return false;
    if (!nextTokenIs(b, "", PLUS, MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, PLUS_EXPR, null);
    r = plus_op(b, l + 1);
    r = r && factor(b, l + 1, _p);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '+'|'-'
  static boolean plus_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plus_op")) return false;
    if (!nextTokenIs(b, "", PLUS, MINUS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // message_term | literal_expr | <<ref_expr <<p>>>> | function_call  | paren_expr
  static boolean primary(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "primary")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = message_term(b, l + 1);
    if (!r) r = literal_expr(b, l + 1);
    if (!r) r = ref_expr(b, l + 1, _p);
    if (!r) r = function_call(b, l + 1);
    if (!r) r = paren_expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (namespace_prefix (new_line* !assignments_and_keywords))? MINUS? (function_call | identifier | environment_id | (!<<p>> keyword_list) )
  public static boolean ref_expr(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "ref_expr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ref_expr_0(b, l + 1);
    r = r && ref_expr_1(b, l + 1);
    r = r && ref_expr_2(b, l + 1, _p);
    exit_section_(b, m, REF_EXPR, r);
    return r;
  }

  // (namespace_prefix (new_line* !assignments_and_keywords))?
  private static boolean ref_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ref_expr_0")) return false;
    ref_expr_0_0(b, l + 1);
    return true;
  }

  // namespace_prefix (new_line* !assignments_and_keywords)
  private static boolean ref_expr_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ref_expr_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namespace_prefix(b, l + 1);
    r = r && ref_expr_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // new_line* !assignments_and_keywords
  private static boolean ref_expr_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ref_expr_0_0_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ref_expr_0_0_1_0(b, l + 1);
    r = r && ref_expr_0_0_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // new_line*
  private static boolean ref_expr_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ref_expr_0_0_1_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!new_line(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ref_expr_0_0_1_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // !assignments_and_keywords
  private static boolean ref_expr_0_0_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ref_expr_0_0_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !assignments_and_keywords(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // MINUS?
  private static boolean ref_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ref_expr_1")) return false;
    consumeToken(b, MINUS);
    return true;
  }

  // function_call | identifier | environment_id | (!<<p>> keyword_list)
  private static boolean ref_expr_2(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "ref_expr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = function_call(b, l + 1);
    if (!r) r = consumeToken(b, IDENTIFIER);
    if (!r) r = environment_id(b, l + 1);
    if (!r) r = ref_expr_2_3(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // !<<p>> keyword_list
  private static boolean ref_expr_2_3(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "ref_expr_2_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ref_expr_2_3_0(b, l + 1, _p);
    r = r && keyword_list(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !<<p>>
  private static boolean ref_expr_2_3_0(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "ref_expr_2_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !_p.parse(b, l);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // SHEBANG_LINE
  public static boolean shebang(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shebang")) return false;
    if (!nextTokenIs(b, SHEBANG_LINE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SHEBANG_LINE);
    exit_section_(b, m, SHEBANG, r);
    return r;
  }

  /* ********************************************************** */
  // &(keyword_list TILDE) expr
  public static boolean standalone_keyword_message_term(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standalone_keyword_message_term")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STANDALONE_KEYWORD_MESSAGE_TERM, "<standalone keyword message term>");
    r = standalone_keyword_message_term_0(b, l + 1);
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // &(keyword_list TILDE)
  private static boolean standalone_keyword_message_term_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standalone_keyword_message_term_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = standalone_keyword_message_term_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // keyword_list TILDE
  private static boolean standalone_keyword_message_term_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "standalone_keyword_message_term_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = keyword_list(b, l + 1);
    r = r && consumeToken(b, TILDE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING_LITERAL | BINARY_STRING_LITERAL | HEX_STRING_LITERAL
  public static boolean string_lit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_lit")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_LIT, "<string lit>");
    r = consumeToken(b, STRING_LITERAL);
    if (!r) r = consumeToken(b, BINARY_STRING_LITERAL);
    if (!r) r = consumeToken(b, HEX_STRING_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<factor <<p>>>> <<plus_expr <<p>>>>*
  static boolean summand(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "summand")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = factor(b, l + 1, _p);
    r = r && summand_1(b, l + 1, _p);
    exit_section_(b, m, null, r);
    return r;
  }

  // <<plus_expr <<p>>>>*
  private static boolean summand_1(PsiBuilder b, int l, Parser _p) {
    if (!recursion_guard_(b, l, "summand_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!plus_expr(b, l + 1, _p)) break;
      if (!empty_element_parsed_guard_(b, "summand_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // STRING_LITERAL | id
  public static boolean symbol_or_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_or_string")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SYMBOL_OR_STRING, "<symbol or string>");
    r = consumeToken(b, STRING_LITERAL);
    if (!r) r = id(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // symbol_or_string | function_call
  public static boolean symbol_or_string_or_function(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_or_string_or_function")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SYMBOL_OR_STRING_OR_FUNCTION, "<symbol or string or function>");
    r = symbol_or_string(b, l + 1);
    if (!r) r = function_call(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // symbol_or_string | keyword_list | lit
  public static boolean symbol_or_string_or_keyword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_or_string_or_keyword")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SYMBOL_OR_STRING_OR_KEYWORD, "<symbol or string or keyword>");
    r = symbol_or_string(b, l + 1);
    if (!r) r = keyword_list(b, l + 1);
    if (!r) r = lit(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ( (expr (COMMA expr)*)+ | COMMA | DOT | ( (EQ | MINUS | PLUS) <<paren expr>>))+
  static boolean template_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = template_list_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!template_list_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "template_list", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (expr (COMMA expr)*)+ | COMMA | DOT | ( (EQ | MINUS | PLUS) <<paren expr>>)
  private static boolean template_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = template_list_0_0(b, l + 1);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, DOT);
    if (!r) r = template_list_0_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (expr (COMMA expr)*)+
  private static boolean template_list_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = template_list_0_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!template_list_0_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "template_list_0_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // expr (COMMA expr)*
  private static boolean template_list_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr(b, l + 1);
    r = r && template_list_0_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMA expr)*
  private static boolean template_list_0_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0_0_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!template_list_0_0_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "template_list_0_0_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA expr
  private static boolean template_list_0_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (EQ | MINUS | PLUS) <<paren expr>>
  private static boolean template_list_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = template_list_0_3_0(b, l + 1);
    r = r && paren(b, l + 1, expr_parser_);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQ | MINUS | PLUS
  private static boolean template_list_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "template_list_0_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, PLUS);
    exit_section_(b, m, null, r);
    return r;
  }

  final static Parser DUMMYOBJECT_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return consumeToken(b, DUMMYOBJECT);
    }
  };
  final static Parser FOR_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return consumeToken(b, FOR);
    }
  };
  final static Parser THEN_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return consumeToken(b, THEN);
    }
  };
  final static Parser WITH_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return consumeToken(b, WITH);
    }
  };
  final static Parser array_term_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return array_term(b, l + 1);
    }
  };
  final static Parser directive_recover_rule_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return directive_recover_rule(b, l + 1);
    }
  };
  final static Parser expr_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return expr(b, l + 1);
    }
  };
  final static Parser keyword_do_conditional_1_0_0_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_do_conditional_1_0_0_0(b, l + 1);
    }
  };
  final static Parser keyword_do_repetitor_2_for_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_do_repetitor_2_for_1_0(b, l + 1);
    }
  };
  final static Parser keyword_do_repetitor_assign_2_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_do_repetitor_assign_2_0(b, l + 1);
    }
  };
  final static Parser keyword_do_repetitor_by_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_do_repetitor_by_1_0(b, l + 1);
    }
  };
  final static Parser keyword_do_repetitor_for_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_do_repetitor_for_1_0(b, l + 1);
    }
  };
  final static Parser keyword_do_repetitor_to_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_do_repetitor_to_1_0(b, l + 1);
    }
  };
  final static Parser keyword_forward_arguments_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_forward_arguments_1_0(b, l + 1);
    }
  };
  final static Parser keyword_forward_class_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_forward_class_1_0(b, l + 1);
    }
  };
  final static Parser keyword_forward_message_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_forward_message_1_0(b, l + 1);
    }
  };
  final static Parser keyword_forward_to_1_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_forward_to_1_1_0(b, l + 1);
    }
  };
  final static Parser keyword_forward_to_2_2_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_forward_to_2_2_0(b, l + 1);
    }
  };
  final static Parser keyword_instruction_recovery_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_instruction_recovery(b, l + 1);
    }
  };
  final static Parser keyword_list_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_list(b, l + 1);
    }
  };
  final static Parser keyword_raise_options_additional_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_raise_options_additional_1_0(b, l + 1);
    }
  };
  final static Parser keyword_raise_options_description_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_raise_options_description_1_0(b, l + 1);
    }
  };
  final static Parser keyword_raise_options_exit_return_0_1_0_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_raise_options_exit_return_0_1_0_0(b, l + 1);
    }
  };
  final static Parser keyword_raise_options_exit_return_1_1_0_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_raise_options_exit_return_1_1_0_0(b, l + 1);
    }
  };
  final static Parser keyword_select_case_1_0_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return keyword_select_case_1_0(b, l + 1);
    }
  };
  final static Parser symbol_or_string_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return symbol_or_string(b, l + 1);
    }
  };
}
