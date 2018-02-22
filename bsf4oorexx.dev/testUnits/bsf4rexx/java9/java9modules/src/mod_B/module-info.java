// rgf, 2018-01-14: module "mod1" exports all of its classes by exporting the package named "mtest1"
// cannot be compiled on its own?

module mod_B {
// open module mod_B {     // this will make the module fully reflectable!
    requires mod_A;

    exports mtest2 to mod_C;
    // opens   mtest2;     // open for full reflection (otherwise only public members)
}
