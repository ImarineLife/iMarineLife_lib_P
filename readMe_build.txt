The dependency libraries aars were built by adding them to settings.gradle
The aars were put in the libs directory of iMarineLife_lib
hoping they would be included into the iMarinelife_lib aar
but that does not help either

I just put the aars into the libs of the application project (MOO3)
That works
So I just put the settings.gradle back
because that also creates the aars for the downloader, zp and play_licensing libraries

transitive will include any jars in the libs directory or from any Maven repository
but NOT the classes from an included aar
