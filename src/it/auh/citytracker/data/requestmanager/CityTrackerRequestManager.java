/**
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package it.auh.citytracker.data.requestmanager;

import com.foxykeep.datadroid.requestmanager.RequestManager;
import it.auh.citytracker.data.service.CityTrackerRequestService;

import android.content.Context;

/**
 * This class is used as a proxy to call the Service. It provides easy-to-use methods to call the
 * service and manages the Intent creation. It also assures that a request will not be sent again if
 * an exactly identical one is already in progress.
 *
 * @author Foxykeep, Francesco Pontillo
 */
public final class CityTrackerRequestManager extends RequestManager {

    // Singleton management
    private static CityTrackerRequestManager sInstance;

    public synchronized static CityTrackerRequestManager from(Context context) {
        if (sInstance == null) {
            sInstance = new CityTrackerRequestManager(context);
        }

        return sInstance;
    }

    private CityTrackerRequestManager(Context context) {
        super(context, CityTrackerRequestService.class);
    }
}
