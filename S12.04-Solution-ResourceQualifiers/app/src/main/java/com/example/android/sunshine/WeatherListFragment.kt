package com.example.android.sunshine

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.sunshine.data.WeatherContract
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.widget.ProgressBar
import com.example.android.sunshine.sync.SunshineSyncUtils

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class WeatherListFragment : Fragment(), ForecastAdapter.ForecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {
    /*
     * This ID will be used to identify the Loader responsible for loading our weather forecast. In
     * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
     * We will still use this ID to initialize the loader and create the loader for best practice.
     * Please note that 44 was chosen arbitrarily. You can use whatever number you like, so long as
     * it is unique and consistent.
     */
    private val ID_FORECAST_LOADER = 44

    companion object {
        /*
    * We store the indices of the values in the array of Strings above to more quickly be able to
    * access the data from our query. If the order of the Strings above changes, these indices
    * must be adjusted to match the order of the Strings.
    */
        val INDEX_WEATHER_DATE = 0
        val INDEX_WEATHER_MAX_TEMP = 1
        val INDEX_WEATHER_MIN_TEMP = 2
        val INDEX_WEATHER_CONDITION_ID = 3

        /*
         * The columns of data that we are interested in displaying within our MainActivity's list of
         * weather data.
         */
        val MAIN_FORECAST_PROJECTION =
                arrayOf(WeatherContract.WeatherEntry.COLUMN_DATE,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
                )

    }

    private var mForecastAdapter: ForecastAdapter? = null

    /**
     * This method is for responding to clicks from our list.
     *
     * @param date Normalized UTC time that represents the local date of the weather in GMT time.
     * @see WeatherContract.WeatherEntry.COLUMN_DATE
     */
    override fun onClick(date: Long) {
//        val weatherDetailIntent = Intent(this@MainActivity, DetailActivity::class.java)
//        val uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date)
//        weatherDetailIntent.data = uriForDateClicked
//        startActivity(weatherDetailIntent)
    }

    /**
     * Called by the [android.support.v4.app.LoaderManagerImpl] when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    override fun onCreateLoader(loaderId: Int, bundle: Bundle?): Loader<Cursor> {
        when (loaderId) {

            ID_FORECAST_LOADER -> {
                /* URI for all rows of weather data in our weather table */
                val forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI
                /* Sort order: Ascending by date */
                val sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC"
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                val selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards()

                return CursorLoader(this.context,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder)
            }

            else -> throw RuntimeException("Loader Not Implemented: " + loaderId)
        }
    }

    /**
     * Called when a Loader has finished loading its data.
     *
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mForecastAdapter?.swapCursor(data)
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0
        mRecyclerView?.smoothScrollToPosition(mPosition)
        if (data.count != 0) showWeatherDataView()
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     *
     *
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private fun showWeatherDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator?.setVisibility(View.INVISIBLE)
        /* Finally, make sure the weather data is visible */
        mRecyclerView?.setVisibility(View.VISIBLE)
    }

    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     *
     *
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private fun showLoading() {
        /* Then, hide the weather data */
        mRecyclerView?.setVisibility(View.INVISIBLE)
        /* Finally, show the loading indicator */
        mLoadingIndicator?.setVisibility(View.VISIBLE)
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    override fun onLoaderReset(loader: Loader<Cursor>) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mForecastAdapter?.swapCursor(null)
    }

    // TODO: Customize parameters
    private var mColumnCount = 1
    //    private var mListener: OnListFragmentInteractionListener? = null
    private var mRecyclerView: RecyclerView? = null
    private var mPosition = RecyclerView.NO_POSITION
    private var mLoadingIndicator: ProgressBar? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_item_list, container, false)

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = view.findViewById(R.id.recyclerview_forecast) as RecyclerView

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = view.findViewById(R.id.pb_loading_indicator) as ProgressBar

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        val layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        (mRecyclerView as RecyclerView).setLayoutManager(layoutManager)

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        (mRecyclerView as RecyclerView).setHasFixedSize(true)

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mForecastAdapter = ForecastAdapter(this.context, this)

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        (mRecyclerView as RecyclerView).setAdapter(mForecastAdapter)

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        activity.supportLoaderManager.initLoader(ID_FORECAST_LOADER, null, this)

        SunshineSyncUtils.initialize(this.context)

        showLoading()
        return view
    }
}
