/**
 * 2012 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package it.auh.citytracker.data.requestmanager;

import com.foxykeep.datadroid.requestmanager.Request;

/**
 * Class used to create the {@link Request}s.
 *
 * @author Foxykeep, Francesco Pontillo
 */
public final class CityTrackerRequestFactory {

    // Request types
    public static final int REQUEST_POST_REPORT = 0;
    public static final int REQUEST_GET_REPORT = 1;

    // Response data
    public static final String BUNDLE_USER = "it.auh.citytracker.extra.USER";
    public static final String BUNDLE_DESC = "it.auh.citytracker.extra.DESC";

    private CityTrackerRequestFactory() {
        // no public constructor
    }

    /**
     * Create the send report request.
     * @return The request.
     */
    public static Request getPostRequest() {
        Request request = new Request(REQUEST_POST_REPORT);
        return request;
    }
    
    /**
     * Creates the get report request.
     * @return The request.
     */
    public static Request getGetRequest() {
        Request request = new Request(REQUEST_GET_REPORT);
        return request;
    }

}
