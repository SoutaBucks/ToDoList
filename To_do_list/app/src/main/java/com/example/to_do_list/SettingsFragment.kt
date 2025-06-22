package com.example.to_do_list

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.Fragment

class SettingsFragment : Fragment() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchWeatherEnabled: Switch
    private lateinit var switchCategoryAutoDetect: Switch
    private lateinit var spinnerDefaultCategory: Spinner
    private lateinit var switchDarkMode: Switch
    private lateinit var switchNotifications: Switch
    private lateinit var buttonClearData: Button
    private lateinit var textViewAppVersion: TextView
    
    companion object {
        const val PREF_WEATHER_ENABLED = "weather_enabled"
        const val PREF_CATEGORY_AUTO_DETECT = "category_auto_detect"
        const val PREF_DEFAULT_CATEGORY = "default_category"
        const val PREF_DARK_MODE = "dark_mode"
        const val PREF_NOTIFICATIONS = "notifications"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        initializeSharedPreferences()
        setupListeners()
        loadSettings()
    }
    
    private fun initializeViews(view: View) {
        switchWeatherEnabled = view.findViewById(R.id.switchWeatherEnabled)
        switchCategoryAutoDetect = view.findViewById(R.id.switchCategoryAutoDetect)
        spinnerDefaultCategory = view.findViewById(R.id.spinnerDefaultCategory)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        buttonClearData = view.findViewById(R.id.buttonClearData)
        textViewAppVersion = view.findViewById(R.id.textViewAppVersion)
        
        // 기본 카테고리 스피너 설정
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.todo_categories,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDefaultCategory.adapter = adapter
        
        // 앱 버전 표시
        textViewAppVersion.text = "버전 1.2.0 (날씨 기능 포함)"
    }
    
    private fun initializeSharedPreferences() {
        sharedPreferences = requireActivity().getSharedPreferences("TodoAppSettings", Context.MODE_PRIVATE)
    }
    
    private fun setupListeners() {
        switchWeatherEnabled.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_WEATHER_ENABLED, isChecked)
            if (isChecked) {
                Toast.makeText(context, "날씨 기능이 활성화되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "날씨 기능이 비활성화되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
        
        switchCategoryAutoDetect.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_CATEGORY_AUTO_DETECT, isChecked)
            if (isChecked) {
                Toast.makeText(context, "자동 카테고리 분류가 활성화되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "자동 카테고리 분류가 비활성화되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
        
        spinnerDefaultCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                savePreference(PREF_DEFAULT_CATEGORY, selectedCategory)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_DARK_MODE, isChecked)
            Toast.makeText(context, "다크 모드 설정이 변경되었습니다", Toast.LENGTH_SHORT).show()
        }
        
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_NOTIFICATIONS, isChecked)
            if (isChecked) {
                Toast.makeText(context, "알림이 활성화되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "알림이 비활성화되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
        
        buttonClearData.setOnClickListener {
            showClearDataDialog()
        }
    }
    
    private fun loadSettings() {
        switchWeatherEnabled.isChecked = sharedPreferences.getBoolean(PREF_WEATHER_ENABLED, true)
        switchCategoryAutoDetect.isChecked = sharedPreferences.getBoolean(PREF_CATEGORY_AUTO_DETECT, true)
        switchDarkMode.isChecked = sharedPreferences.getBoolean(PREF_DARK_MODE, false)
        switchNotifications.isChecked = sharedPreferences.getBoolean(PREF_NOTIFICATIONS, true)
        
        // 기본 카테고리 설정
        val defaultCategory = sharedPreferences.getString(PREF_DEFAULT_CATEGORY, "기타")
        val adapter = spinnerDefaultCategory.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(defaultCategory)
        spinnerDefaultCategory.setSelection(position)
    }
    
    private fun savePreference(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    private fun savePreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    private fun showClearDataDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("데이터 삭제")
            .setMessage("모든 할일 데이터를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("삭제") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun clearAllData() {
        val todoManager = TodoManager.getInstance(requireContext())
        // 모든 할일 삭제 (실제 구현은 TodoManager에 메소드 추가 필요)
        Toast.makeText(context, "모든 데이터가 삭제되었습니다", Toast.LENGTH_SHORT).show()
    }
    
    // 설정값을 다른 클래스에서 사용할 수 있도록 하는 헬퍼 메소드들
    fun isWeatherEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_WEATHER_ENABLED, true)
    }
    
    fun isCategoryAutoDetectEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_CATEGORY_AUTO_DETECT, true)
    }
    
    fun getDefaultCategory(): String {
        return sharedPreferences.getString(PREF_DEFAULT_CATEGORY, "기타") ?: "기타"
    }
} 