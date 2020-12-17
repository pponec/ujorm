package org.ujorm.ujoservlet.ajax.ao;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.ujorm.tools.web.ao.WebUtils;

/**
 *
 * @author Pavel Ponec
 */
public class CityResourceService {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(CityResourceService.class.toString());
    private static final String CITY_CSV = "/csv/ResourceCity.csv";

    private Map<Integer,City> cityMap = null;

    @Nonnull
    public City getCity(String id) {
        return getCity(Integer.valueOf(id));
    }

    @Nonnull
    public City getCity(Integer id) {
        try {
            final City result = getCityMap().get(id);
            return result != null
                    ? result
                    : new City();
        } catch (IOException e) {
            return new City();
        }
    }

    public Map<Integer,City> getCityMap() throws IOException {
        if (cityMap == null) {
            synchronized (this) {
                try (Stream<City> cityStream =  loadCityStream()) {
                    cityMap = cityStream.collect(Collectors.toMap(City::getId, Function.identity()));
                    Object a = "x";
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "City reading fails", e);
                    return Collections.emptyMap();
                }
            }
        }
        return cityMap;
    }

    /**
     * Direct stream of data source.
     * @return
     */
    public Stream<City> loadCityStream() throws IOException {
        return loadCitys(getClass().getResource(CITY_CSV));
    }

    /**
     * Return a raw stream.
     *
     * Read file into stream, try-with-resources.
     *
     * ( City.ID
     * , City.NAME
     * , City.COUNTRY
     * , City.COUNTRY_NAME
     * , City.LATITUDE
     * , City.LONGITUDE
     *
     * @return
     */
    protected Stream<City> loadCitys(URL url) throws IOException {

        return WebUtils.rowsOfUrl(url)
                .filter(t -> !t.startsWith("* "))
                .filter(t -> !t.startsWith("ID;"))
                .map(t -> {
                    City city = null;
                    String[] c = t.split(";");
                    if (c.length > 5) {
                        city = new City();
                        city.setId(Integer.valueOf(c[0]));
                        city.setName(c[1]);
                        city.setCountry(c[2]);
                        city.setCountryName(c[3]);
                        city.setLatitude(Float.parseFloat(c[4]));
                        city.setLongitude(Float.parseFloat(c[5]));
                    }
                    return city;
                })
                .filter(t -> t != null);
    }

}
