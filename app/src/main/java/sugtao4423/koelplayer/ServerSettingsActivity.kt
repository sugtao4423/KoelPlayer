package sugtao4423.koelplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import sugtao4423.koelplayer.databinding.ActivityServerSettingsBinding
import sugtao4423.koelplayer.viewmodel.ServerSettingsViewModel

class ServerSettingsActivity : AppCompatActivity() {

    companion object {
        const val INTENT_KEY_IS_RE_AUTH = "isReAuth"
    }

    private lateinit var binding: ActivityServerSettingsBinding

    private val viewModel: ServerSettingsViewModel by viewModels()

    private val isReAuth by lazy {
        intent.getBooleanExtra(INTENT_KEY_IS_RE_AUTH, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
        viewModel.initialize(isReAuth)
    }

    private fun initViews() {
        if (isReAuth) {
            binding.serverHost.isEnabled = false
        }

        binding.fab.setOnClickListener {
            viewModel.authenticate(
                binding.serverHost.text.toString(),
                binding.serverEmail.text.toString(),
                binding.serverPassword.text.toString(),
            )
        }
    }

    private fun initObservers() {
        viewModel.serverHost.observe(this) {
            binding.serverHost.setText(it)
        }

        viewModel.authState.observe(this) {
            when (it) {
                ServerSettingsViewModel.AuthState.Idle -> {
                    binding.fab.isEnabled = true
                }

                ServerSettingsViewModel.AuthState.Loading -> {
                    binding.fab.isEnabled = false
                }

                ServerSettingsViewModel.AuthState.Success -> {
                    if (isReAuth) {
                        finish()
                    } else {
                        SyncMusicData(this).sync {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                }

                ServerSettingsViewModel.AuthState.AuthError -> {
                    errorGetToken()
                    binding.fab.isEnabled = true
                }
            }
        }
    }

    private fun errorGetToken() {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.error_get_token)
            show()
        }
    }

    fun hideKeyboard(v: View) {
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }

}
