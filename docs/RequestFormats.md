# Introduction #

The servlet provides holding (item and copy) information from HIP.

You can look things up by bibID, itemID, copyID, or barcode.  You can request all information for a given bib, or just information related to a certain item or copy.

You can get the information returned in several different formats: a uchicago ad hoc XML format, or the DLF ils-di XML format.  For DLF ils-di format, several different metadata 'payloads' are provided, and you can limit the result to only certain ones if you like, for a smaller response.

There are also several different URL formats that the servlet can accept:  REST-style, uchicago query paramater style, or DLF 'GetAvailability' query parameter style.

# Base URL #

By default the servlet installs in JBoss at /ws.  And the items/holdings components is available at /ws/items.

So if your HIP instance is normally found at http://someserver.com/ipac20, then the base URL for the items-holdings service would be http://someserver.com/ws/holdings

This JBoss 'mount point' is set in the source at src/metadata/ws-web.xml.

# Request types #

## Fetch holdings for a bib ##

REST style
  * /ws/holdings/bib/1111111
uchicago query param
  * /ws/holdings?bib=1111111
DLF getAvailability
  * /ws/holdings/availability?id=111111&id\_type=bib
  * Note we only support one bibID included in id

By default all items and/or copies attached to that bib will be included in result, but see includeItems parameter below.

## Fetch holdings for a copy ##

REST style
  * /ws/holdings/copy/11111
uchicago query param
  * /ws/holdings?copy=11111
DLF getAvailability
  * /ws/holdings/availability?id=1111&id\_type=copy
  * id\_type=copy isn't officially part of the DLF api, but we support it anyway in that style.

By default all items attached to this copy will be included in result, but see includeItems parameter below.  Only this copy will be included, not other copies attached to the same bib.

## Fetch holdings for item ##

By itemID or barcode

REST style
  * /ws/holdings/item/11111
  * /ws/holdings/barcode/39483434
uchicago query param
  * /items?item=11111
  * /items?barcode=39483434
DLF getAvailability style
  * /items/availability?id=1111&id\_type=item
  * /items/availability?id=39483434&id\_type=barcode
  * id\_type=barcode isn't mentioned by DLF, but we support that style anyway.

Only the individual item referenced will be included in response, not other items attached to the same copy or bib.

# Other parameters #

## Response format ##

Response can be in uchicago ad hoc XML format, or in DLF ils-di 'dlfexpanded' format. By default the format is DLF ils-di 'dlfexpanded', but this default can be changed in config/general.properties.

To request a specific format, in REST style, eg:
  * /items/bib/11111.uchicago
  * /items/bib/11111.dlfexpanded

To request a specific format in any style with query parameter:
  * /items?bib=11111&format=uchicago
  * /items?bib=11111&format=dlfexpanded

## includeItems ##

If a request includes a copy, and the copy has items, then by default all of the attached items will be included in the response too.  (This default can be changed in config/general.properties holdings.default\_include\_items).

However, sometimes you don't actually need the items, and including the items can lead to slightly slower performance and (in some cases) greatly increased size of response. You can force the servlet not to include subsidiary items with includeItems=false.

  * /items/bib/109?includeItems=false
  * /items?copy=204&includeItems=false

Note that this will only have an effect if the response has 'copies' in it.  If bib 109 in the above example doesn't have copy records but only item records, includeItems will have no effect. If bib 109 has copy records attached, then includeItems=false will mean only those copy records will be included in the response, not any item records attached to those copy records.

## DLF metadata payloads ##

The DLF 'dlfexpanded' schema is actually only a skeleton, which carries arbitrary metadata payloads to actually express information about the copies (called 'holdingsets' in DLF terminology) and items included in the response.

The servlet includes metadata payloads in multiple formats to fully express all information you might want about a holding.  See [ResponseFormats](ResponseFormats.md).

By default, all available metadata formats will be included, but you can use query parameters to limit to only certain formats. This is useful if you don't need certain formats, to improve performance and limit response size.

Examples:
```
  /items/bib/109&itemFormat=simpleAvailability,mfhd,ilsDetails,daia&copyFormat=mfhd,ilsDetails
  /items/bib=109&copyFormat=&itemFormat=simpleAvailability
```

Note that an empty string for copyFormat or itemFormat will mean that no metadata formats will be included for those holdings.  However, the copies and items will still be listed, just without any description, with only an id.

## Note on Multiple ID's ##

All request formats can take multiple IDs, comma-seperated, eg:

```
  /ws/holdings/bib/109,110,111
  /ws/holdings?copy=1111111,123,124
  /ws/holdings/availability?id=111,222&id_type=bib
```

When querring for multiple items, XML response is wrapped in an aggregator element ("dlf:collection" or "results").  And if any or all of the IDs are not found, **no** error will be raised, the missing entities will simply not be included. Compare to single-id-request, where if the id is not found a 404 HTTP status will be returned.