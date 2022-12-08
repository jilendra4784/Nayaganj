package naya.ganj.app

import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.paulrybitskyi.persistentsearchview.PersistentSearchView
import com.paulrybitskyi.persistentsearchview.utils.VoiceRecognitionDelegate

class GlobalSearchActivity : AppCompatActivity() {

    lateinit var persistentSearchView: PersistentSearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_search)

        persistentSearchView = findViewById(R.id.persistentSearchView) as PersistentSearchView
        with(persistentSearchView) {
            setOnLeftBtnClickListener {
                // Handle the left button click
                Log.e("TAG", "setOnLeftBtnClickListener: ")
            }
            setOnClearInputBtnClickListener {
                // Handle the clear input button click
                Log.e("TAG", "setOnClearInputBtnClickListener: ")
            }

            // Setting a delegate for the voice recognition input
            setVoiceRecognitionDelegate(VoiceRecognitionDelegate(this@GlobalSearchActivity))
            isVoiceInputButtonEnabled = true
            setOnSearchConfirmedListener { searchView, query ->
                // Handle a search confirmation. This is the place where you'd
                // want to perform a search against your data provider.
                Log.e("TAG", "setOnClearInputBtnClickListener: " + query)
                SearchRecentSuggestions(
                    this@GlobalSearchActivity,
                    MySuggestionProvider.AUTHORITY,
                    MySuggestionProvider.MODE
                )
                    .saveRecentQuery(query, null)
            }

            // Disabling the suggestions since they are unused in
            // the simple implementation
            setSuggestionsDisabled(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        VoiceRecognitionDelegate.handleResult(persistentSearchView, requestCode, resultCode, data);
    }

    override fun onResume() {
        super.onResume()

        /*  val searchQueries = if(persistentSearchView.isInputQueryEmpty) {
              mDataProvider.getInitialSearchQueries()
          } else {
              mDataProvider.getSuggestionsForQuery(persistentSearchView.inputQuery)
          }

          // Converting them to recent suggestions and setting them to the widget
          persistentSearchView.setSuggestions(SuggestionCreationUtil.asRecentSearchSuggestions(searchQueries), false)
      */
    }
}