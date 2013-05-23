/**
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package it.auh.citytracker.data.service;

import it.auh.citytracker.data.operation.PostReportOperation;
import it.auh.citytracker.data.requestmanager.CityTrackerRequestFactory;

import com.foxykeep.datadroid.service.RequestService;

import android.content.Intent;

/**
 * This class is called by the {@link CityTrackerRequestManager} through the {@link Intent} system.
 *
 * @author Foxykeep, Francesco Pontillo
 */
public final class CityTrackerRequestService extends RequestService {

    @Override
    protected int getMaximumNumberOfThreads() {
        return 5;
    }

    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType) {
            case CityTrackerRequestFactory.REQUEST_GET_REPORT:
                return null;
            case CityTrackerRequestFactory.REQUEST_POST_REPORT:
                return new PostReportOperation();
        }
        return null;
    }
}
