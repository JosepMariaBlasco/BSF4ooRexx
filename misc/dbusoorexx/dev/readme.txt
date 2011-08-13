rgf, 2011-06-02

- Setup needed:

        - unpack the package, switch into it

        - get "libexpat1-dev"
        - get "libdbus-1-dev"

        - ./configure

         on Ubuntu 11.04:

         cc -g -Wall `pkg-config --libs --cflags dbus-1` -o dbus-example dbus-example.c

--

        - to compile 32-bits on 64-bit Linux:

          sudo apt-get install libc6-dev-i386           -- for 32-bit library functions
          sudo apt-get install g++-multilib             -- for 32-bit libs

---

        <?xml version="1.0" encoding="UTF-8" ?>
        <!DOCTYPE busconfig PUBLIC
          "-//freedesktop//DTD D-BUS Bus Configuration 1.0//EN"
          "http://www.freedesktop.org/standards/dbus/1.0/busconfig.dtd">

        <busconfig>

        <!-- rgf, 2011-06-11: - for developing and testing purposes allow the users 'root', 'wu', 'rony' to
                                own all bus names and to invoke "method_call"s; rights that are denied by
                                default according to /etc/dbus-1/system.conf
                              - this file should go as/to /etc/dbus-1/system-local.conf (last config file
                                that gets processed, hence can override any other previous definitions

        -->

           <policy user="root">
               <allow own="*"/>
               <allow send_type="method_call"/>
           </policy>

           <policy user="rony">
               <allow own="*"/>
               <allow send_type="method_call"/>
           </policy>

           <policy user="wu">
               <allow own="*"/>
               <allow send_type="method_call"/>
           </policy>

        <!-- let console programs do everything -->
        <!--
          <policy at_console="true">
               <allow own="*"/>
               <allow send_type="method_call"/>
          </policy>
        -->

        </busconfig>


----

if creating the package oneself (ATTENTION: do not "make install" this on Linux, it may break the system!) :
        ./configure

        alternatively: ./configure `pkg-config --libs --cflags dbus-1`

        make
        make install


----

If security problem, then cf. <http://www.linuxquestions.org/questions/linux-software-2/dbus-problem-505442/>:

        Solved!

        i have gone thru the "/etc/dbus-1/system.conf" and found these lines
        Quote:

                <!-- Config files are placed here that among other things, punch
                holes in the above policy for specific services. -->
                <includedir>system.d</includedir>

                <!-- This is included last so local configuration can override what's
                in this standard file -->
                <include ignore_missing="yes">system-local.conf</include>

        and hence i created the system-local.conf and modified as below
        Quote:

                <policy context="default">
                <!-- Deny everything then punch holes -->
                <allow send_interface="*"/>
                <allow receive_interface="*"/>
                <allow own="*"/>
                <!-- But allow all users to connect -->
                <allow user="*"/>
                <!-- Allow anyone to talk to the message bus -->
                <!-- FIXME I think currently these allow rules are always implicit
                even if they aren't in here -->
                <allow send_destination="org.freedesktop.DBus"/>
                <allow receive_sender="org.freedesktop.DBus"/>
                <!-- valid replies are always allowed -->
                <allow send_requested_reply="true"/>
                <allow receive_requested_reply="true"/>
                </policy>

        to rectify this issue

        Note: donot forget to remove the

        Quote:

        <include ignore_missing="yes">system-local.conf</include>
        in the newly created system-local.conf file


--- to build dbus from the net, cf. *excellent* tutorial (handouts-pdf) <http://www.lrde.epita.fr/~adl/autotools.html> as of 2011-08-13:

        - autotools
        - run autoreconf (runs all the tools in the right order):
                autoreconf --install --force

        - create Makefile, install into $HOME
                ./configure --prefix $HOME

        - then
             make [all]
             make install               ... install into prefix-dir
             make install-strip         ... install but strip debugging symbols

             make uninstall             ... uninstall
             make clean                 ... erase what has been built
             make distclean             ... removes all ./configure created stuff

             make dist                  ... create tar.gz package

             make check                 ... run the test suite, if any
             make installcheck          ... check the installed programs or libraries, if supported


