After running gradle task fragility-mapping:jettyRun,
you should be able to do a get request to

http://localhost:8080/fragilitymapping/api/mapping/byJson?json={%20%22no_stories%22:%204,%20%22year_built%22:%201980,%20%22Soil%22:%20%22Upland%22,%20%22occ_type%22:%20%22COM4%22,%20%22struct_typ%22:%20%22C1%22%20,%22retrofit%22:%22Non-Retrofit%20Fragility%20ID%20Code%22}
(or more readably)
http://localhost:8080/fragilitymapping/api/mapping/byJson?json={ "no_stories": 4, "year_built": 1980, "Soil": "Upland", "occ_type": "COM4", "struct_typ": "C1" ,"retrofit":"Non-Retrofit Fragility ID Code"}

and it will return
{
 fragilityId: "STR_C1_5"
}

Note that the "retrofit" parameter doesn't
come from the inventory item itself, but is
data that comes from analysis parameters or elsewhere, and is
used to look up the fragility curve based
on the map of different retrofit levels from the mapping match.