import org.rexxla.bsf.engines.rexx.RexxProxy;

class TestFromCFS extends TestConstrFromSubclass
{
    TestFromCFS(RexxProxy handler)
    {
        super(handler);
    }

    void testRgf()
    {
        System.err.println("oha ["+"So, da, la."+"] null instanceof String: "+(null instanceof String));
    }
}
