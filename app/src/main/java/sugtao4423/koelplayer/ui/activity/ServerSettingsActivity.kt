package sugtao4423.koelplayer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import sugtao4423.koelplayer.R
import sugtao4423.koelplayer.data.SyncMusicData
import sugtao4423.koelplayer.databinding.ActivityServerSettingsBinding
import sugtao4423.koelplayer.viewmodel.ServerSettingsViewModel

class ServerSettingsActivity : AppCompatActivity() {

    companion object {
        const val INTENT_KEY_IS_RE_AUTH = "isReAuth"
    }

    private val binding: ActivityServerSettingsBinding by lazy {
        ActivityServerSettingsBinding.inflate(layoutInflater)
    }

    private val viewModel: ServerSettingsViewModel by viewModels()

    private val isReAuth by lazy {
        intent.getBooleanExtra(INTENT_KEY_IS_RE_AUTH, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        optimizeEdgeToEdge()
        setContentView(binding.root)

        initViews()
        initObservers()
        viewModel.initialize(isReAuth)
    }

    private fun optimizeEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val i =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = maxOf(i.bottom, ime.bottom))
            insets
        }
    }

    private fun initViews() {
        if (isReAuth) {
            binding.serverHost.isEnabled = false
        }

        binding.root.setOnClickListener { hideKeyboard(it) }

        binding.fab.setOnClickListener {
            viewModel.authenticate(
                binding.serverHost.text.toString(),
                binding.serverEmail.text.toString(),
                binding.serverPassword.text.toString(),
            )
        }
    }

    private fun hideKeyboard(v: View) {
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetState()
    }

}
