# iMarineLife_lib_P
This project is an android library project and provides the basic functionality for a fieldguide 
and a log for divers where you can also indicate which species have been spotted during a specific dive. 
It was initially created to support the 'Anemone' foundation (stichting Anemoon) 
which uses recreational divers in the netherlands to collect biodiversity data in the saltwater environment of Zeeland in the Netherlands.

The library is sufficiently general to support any kind of nature watching.
A different app needs to be created for each specific area/fieldguide, where a maximum of 150 species seems to be wise. 
There is now one available for the marine life in the saltwater environment of Zeeland in the Netherlands, 
and one for the sweet water canals of Leiden (Blik Onder Water Leiden)
These two apps are also uploaded to github and can be used as an example.
The apps provide the entrypoint to all the functionality the library provides 
and provide the list of species (latin names, common names, descriptions and groupings, pictures) 
for the fieldguide, locations and app specific tests or choices.

For now the app specific information is internal to the application, which makes it quite large and not very flexible.
The data can be sent to an emailadres as an excel sheet.
