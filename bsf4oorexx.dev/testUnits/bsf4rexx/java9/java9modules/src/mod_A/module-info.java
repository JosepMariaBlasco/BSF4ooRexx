// rgf, 2018-01-14: module "mod1" exports all of its classes by exporting the package named "mtest1"
// cannot be compiled on its own?

module mod_A {
// open module mod_A {
    exports mtest1;
    // opens   mtest1;     // open for full reflection (otherwise only public members)
}
