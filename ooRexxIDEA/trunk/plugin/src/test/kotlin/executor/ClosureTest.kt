package executor

import TestUtils.Companion.assertNoPsiError
import TestUtils.Companion.getPsiFile
import org.junit.jupiter.api.Test

class ClosureTest {

    @Test
    fun testExample1() {
        val ooRexxSource =
            """
{use arg name, greetings
 say "hello" name || greetings
}~("John", ", how are you ?") 
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }


    @Test
    fun testExample2() {
        val ooRexxSource =
            """
nextInteger = {::coactivity end}
say nextInteger~()                  -- 0
say nextInteger~()                  -- 1
nextInteger~makeArray(10)           -- [2,3,4,5,6,7,8,9,10,11]
say nextInteger~()                  -- 12
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample3() {
        val ooRexxSource =
            """
v = 1
closure = {expose v; say v; v += 10}    -- capture the value of v: 1
v = 2
closure~()                              -- display 1
closure~()                              -- display 11
closure~()                              -- display 21
say v                                   -- v not impacted by the closure: 2
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample4() {
        val ooRexxSource =
            """
accumulator = {
    use arg sum
    return  {
        expose sum
        use arg n
        sum += n
        return sum
    }
}

x = accumulator~(1) -- an accumulator (closure), sum initialized to 1
x~(5)               -- add 5 to sum
accumulator~(3)     -- another accumulator (closure), no effect on x
say x~(2.3)         -- add 2.3 to sum and print the current sum : 8.3
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample5() {
        val ooRexxSource =
            """
compose = {
    use arg f, g
    return {
        expose f g
        use arg x
        return f~(g~(x))
    }
}

double = { return 2 * arg(1) }
negative = { return -arg(1) }
say compose~(negative, double)~(5)  -- -10

binary2decimal = compose~("x2d", "b2x")
say binary2decimal~(11111111)  -- 255
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample6() {
        val ooRexxSource =
            """
::class RoutineDoer
::method Y
f = self
return {use arg a ; return a~(a)} ~ {
    expose f ; use arg x
    return f ~ { expose x ; use arg v ; return x~(x)~(v) }
}
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample7() {
        val ooRexxSource =
            """
1~10{ {expose item; return item * item} } ~ take(9) ~ each{ say item~() }
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample8() {
        val ooRexxSource =
            """
a = .array~new
do i=1 to 10
    a~append{expose i; return i*i}
end
do i=1 to 9
    say a[i]~()
end
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample9() {
        val ooRexxSource =
            """
fact = { use arg f
         return  { expose f ; use arg n ; if n == 0 then return 1 ; else return n * f~(n-1) }
       }~Y
say fact~(10) -- 3628800
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample10() {
        val ooRexxSource =
            """
::class RoutineDoer
::method YM
f = self
table = .Table~new
return {use arg a ; return a~(a)} ~ {
    expose f table ; use arg x
    return f ~ { expose x table
                 use arg v
                 r = table[v]
                 if r <> .nil then return r
                 r = x~(x)~(v)
                 table[v] = r
                 return r
               }
}
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }


    @Test
    fun testExample11() {
        val ooRexxSource =
            """
fibm = { use arg fib
         return {expose fib; use arg n
                 if n==0 then return 0
                 if n==1 then return 1
                 if n<0 then return fib~(n+2) - fib~(n+1)
                 return fib~(n-2) + fib~(n-1)
                }
       }~YM
say fibm~(25) -- 75025
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample12() {
        val ooRexxSource =
            """
numeric digits propagate 2090
do i=1 to 100; say "fibm~("i*100")="fibm~(i*100); end
-- fibm~(100)=354224848179261915075
-- fibm~(200)=280571172992510140037611932413038677189525
-- fibm~(300)=222232244629420445529739893461909967206666939096499764990979600
...
-- fibm~(10000)=33644764876431783266621612005107543310302148460680063906564769974680081442166662368155595513633734025582065332680836159373734790483865268263040892463056431887354544369559827491606602099884183933864652731300088830269235673613135117579297437854413752130520504347701602264758318906527890855154366159582987279682987510631200575428783453215515103870818298969791613127856265033195487140214287532698187962046936097879900350962302291026368131493195275630227837628441540360584402572114334961180023091208287046088923962328835461505776583271252546093591128203925285393434620904245248929403901706233888991085841065183173360437470737908552631764325733993712871937587746897479926305837065742830161637408969178426378624212835258112820516370298089332099905707920064367426202389783111470054074998459250360633560933883831923386783056136435351892133279732908133732642652633989763922723407882928177953580570993691049175470808931841056146322338217465637321248226383092103297701648054726243842374862411453093812206564914032751086643394517512161526545361333111314042436854805106765843493523836959653428071768775328348234345557366719731392746273629108210679280784718035329131176778924659089938635459327894523777674406192240337638674004021330343297496902028328145933418826817683893072003634795623117103101291953169794607632737589253530772552375943788434504067715555779056450443016640119462580972216729758615026968443146952034614932291105970676243268515992834709891284706740862008587135016260312071903172086094081298321581077282076353186624611278245537208532365305775956430072517744315051539600905168603220349163222640885248852433158051534849622434848299380905070483482449327453732624567755879089187190803662058009594743150052402532709746995318770724376825907419939632265984147498193609285223945039707165443156421328157688908058783183404917434556270520223564846495196112460268313970975069382648706613264507665074611512677522748621598642530711298441182622661057163515069260029861704945425047491378115154139941550671256271197133252763631939606902895650288268608362241082050562430701794976171121233066073310059947366875
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample13() {
        val ooRexxSource =
            """
"d:\"~pipe(.fileTree recursive.1.breadthFirst | .lineCount {item~parent} | .console {item~right(6)} index)
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }

    @Test
    fun testExample14() {
        val ooRexxSource =
            """
.context~package~pipe(,
    .importedPackages recursive once after mem.package |,
    .inject {item~publicClasses} iterateAfter |,
    .sort {item~id} {dataflow["package"]~item~name} |,
    .console {.file~new(dataflow["package"]~item~name)~name} ":" item,
    )
    """
        val astNode = getPsiFile(ooRexxSource)

        assertNoPsiError(astNode)
    }
}