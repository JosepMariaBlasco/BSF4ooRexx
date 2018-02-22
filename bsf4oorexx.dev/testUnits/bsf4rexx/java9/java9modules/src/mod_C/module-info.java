// rgf, 2018-01-14: module "mod1" exports all of its classes by exporting the package named "mtest1"
// cannot be compiled on its own?

module mod_C {
// open module mod_C {
    exports mtest3;
    requires mod_B;

    // opens   mtest3;     // open for full reflection (otherwise only public members)
}
