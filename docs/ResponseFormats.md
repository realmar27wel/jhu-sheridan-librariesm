# uchicago XML #

This is an ad-hoc XML format developed at uchicago. It doesn't have a schema. It may make sense specifically for Horizon. It does include all the information you might possibly want in a concise and non-redundant fashion however.


# DLF ils-di 'dlfexpanded' #

This is a format developed by the DLF ils-di task force, which is capable of describing Horizon copies (called 'holdingsets' in DLF terminology) and items, and their relationship to each other.  The schema is described here: http://www.diglib.org/architectures/ilsdi/schemas/1.1/dlfexpanded.xsd

Note that while the schema allows you to describe the bib record that's mentioned, this servlet will not do that. The `<dlf:bibliographic>` element will always be empty, only providing the bibID but no further bib-level description. Description is found for copies (holdingsets) and items.  The servlet is not currently capable of providing a bib-level SimpleAvailability response, contrary to the suggestions of the DLF ils-di document.

Note also that depending on what you requested the dlfexpanded response may not include ALL copies and/or items related to the bib mentioned in `<dlf:bibliographic>`. See [RequestFormats](RequestFormats.md)

## holdingset formats ##

A DLF 'holdingset' is the equivalent of a Horizon 'copy' record.  If copies are included in what was requested, DLF `<holdingset>`s will be included in the response for each copy.
Per the DLF spec, each `<holdingset>` element describes what items it contains, but the actual item descriptions are in a separate `<items>` element.

Each `<holdingset>` will have a metadata payload with one or more metadata formats describing that holdingset/copy specifically, as below.

Note copy-level DAIA can NOT currently be provided by the servlet.

### mfhd ###

A marc-xml `<record>` element describing  Marc Format Holdings Data (MFHD) data (http://www.loc.gov/marc/holdings/echdhome.html) with some details on the Horizon Copy.  This is a **very limited** and atypical MFHD response which only returns certain fields necessary to provide the info we want to provide. It will **only** include copy-specific information for one particular copy.

Specifically, it includes:
  * a 'dummy' leader with no real information.
  * copyId in the 001
  * bibId in the 004
  * An 852 field containing the user-displayable location name ($b), collection name ($c), call number ($k, $h, and $i combined).  A note attached to the copy as a whole in 852 $z.  (We previously used 852$a and $b for location and collection, until we realized that $a was reserved for a library code from the Marc list, oops).
  * The type of call number mapped to MFHD values in 852 indicator 1. The servlet will attempt to map to a standard value using the Horizon call\_type.call\_type\_process column, but if it can't figure it out it'll leave the indicator blank.
  * Run statements in 0 or more 866 (main run), 867 (supplement run), and/or 868 (index run). Each run can have a human-readable run-statement ($a) and/or a human readable note ($z).  No machine readable run information is provided, even to the extent MFHD allows, it's too hard to get it from Horizon.

### ilsDetails ###

Additional details about the copy record, primarily including internal Horizon codes for collection, location, item type, etc, are provided in a schema designed for this purpose. The schema can be found here:
http://purl.org/NET/ils-holdings-schema/1
and
http://purl.org/NET/ils-holdings-schema/1/schema

## Item formats ##

### simpleAvailability ###

The `dlf:simpleavailability` element from the dlfexpanded schema provides a human-readable availabilty message, as well as a coded availability status from one of four values: available; not available; possibly available; or unknown.

The servlet by default can only map very few standard Horizon item statuses to 'available' or 'not available' -- all other item statuses will show up as 'unknown'.

You can specify the proper mapping for all of your local item statuses in two ways, using config/general.properties.

  1. Simply list all appropriate item statuses in holdings.dlf\_status\_available, holdings.dlf\_status\_not\_available, holdings.dlf\_status\_possibly\_available

> 2. Add a new column to your Horizon item\_status table to hold the DLF status code. Specify this column in holdings.item\_status\_dlf\_column

### ilsDetails ###

Additional details about the item record, primarily including internal Horizon codes for collection, location, item type, etc, are provided in a schema designed for this purpose. The schema can be found here:
http://purl.org/NET/ils-holdings-schema/1
and
http://purl.org/NET/ils-holdings-schema/1/schema

**note** you can add custom columns from Horizon items table to the ilsdetails response for an item, by editing the config/general.properties properties file to add a holdings.item.localInfo property with a comma-seperated list of columns from Horizon items table. These will each show up as a 'localInfo' element in the response.


### daia ###

A {http://www.gbv.de/wikis/cls/Document_Availability_Information_API_(DAIA) DAIA} record is provided solely to include a direct URL to make a HIP request for this item. This is sadly not yet pre-checked to see if a HIP request is actually possible, the URL is provided for all items.

The Servlet will try to guess the proper HIP request URL. If this is wrong, or you'd like to direct the user to a different non-HIP request form instead, you can set templates for creating a request url in config/general.properties holdings.request\_uri\_template

DAIA requires a URI to represent a bib and an item.  Horizon doesn't really have anything like that, so we try to use URI's pointing back at the servlet itself.  If you'd like to use different URIs to represent bibs or items, templates can be set in config/general.properties holdings.bib\_uri\_template, holdings.copy\_uri\_template, holdings.item\_uri\_template

There is no established service in DAIA to represent this generic 'ILS request', so we use an extension URI to represent that service:  http://purl.org/NET/daia-ext/request

### mfhd ###

This is a marc-xml `<record>` element representing Marc Format for Holdings Data (MFHD) data (http://www.loc.gov/marc/holdings/echdhome.html) expressing certain things **about this specific item**.  Note that this is an atypical use of MFHD, where there isn't usually a separate record per-item, here there is.  We only use certain fields necessary to express what we need about a specific item:

  * A dummy 'leader' that doesn't really say much
  * The containing copyID in 001
  * the containing bibID in 004
  * An 852 field containing the user-displayable location name ($b), collection name ($c), call number ($k, $h, and $i combined), item-specific note ($z) and specific itemID ($p). (We previously used 852$a and $b for location and collection, until we realized that $a was reserved for a library code from the Marc list, oops).
  * The type of call number mapped to MFHD values in 852 indicator 1. The servlet will attempt to map to a standard value using the Horizon call\_type.call\_type\_process column, but if it can't figure it out it'll leave the indicator blank.