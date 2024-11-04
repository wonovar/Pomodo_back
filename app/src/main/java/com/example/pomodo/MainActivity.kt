package com.example.pomodo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Paint
import android.graphics.Typeface
import com.example.pomodo.R
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    private lateinit var addTaskButton: TextView // + 버튼
    private lateinit var taskContainer: LinearLayout // 할 일 목록 컨테이너
    private lateinit var editButton: TextView // 편집 버튼
    private lateinit var calendarIcon: ImageView // 캘린더 아이콘
    private var isEditMode: Boolean = false // 편집 모드 여부를 저장하는 플래그 변수

    private var selectedDate: String = "" // 선택된 날짜를 저장하는 변수

    private val tasksMap = mutableMapOf<String, MutableList<String>>() // 날짜별 할 일 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout) // XML 레이아웃 파일 설정

        // 레이아웃의 뷰 참조
        addTaskButton = findViewById(R.id.addText) // + 버튼 참조
        taskContainer = findViewById(R.id.taskContainer) // 동적으로 할 일 추가할 LinearLayout 참조
        editButton = findViewById(R.id.editButton) // 편집 버튼 참조
        calendarIcon = findViewById(R.id.calendarIcon) // 캘린더 아이콘 참조

        // 편집 버튼 클릭 시 편집 모드 토글
        editButton.setOnClickListener {
            toggleEditMode() // 편집 모드 전환 함수 호출
        }

        // + 버튼 클릭 시 할 일 추가 기능
        addTaskButton.setOnClickListener {
            showAddTaskDialog() // 버튼 클릭 시 새로운 할 일 추가
        }

        // 캘린더 아이콘 클릭 시 날짜 선택 다이얼로그 표시
        calendarIcon.setOnClickListener {
            showDatePickerDialog()
        }
    }

    // 날짜 선택 다이얼로그 표시 함수
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            loadPlannerData(selectedDate) // 선택된 날짜의 할 일 로드
        }, year, month, day)

        datePickerDialog.show()
    }

    // 선택된 날짜에 따라 데이터를 불러오는 함수 (임시 데이터 표시)
    private fun loadPlannerData(selectedDate: String) {
        taskContainer.removeAllViews()

        // 선택한 날짜의 데이터만 깊은 복사하여 새로운 리스트 생성
        val tasks = tasksMap[selectedDate] ?: mutableListOf()

        tasks.forEach { task ->
            addNewTask(task)
        }
    }

    // 할 일과 함께 심볼을 표시할 TextView를 추가하는 함수
    private fun addNewTask(taskName: String) {
        // 새로운 할 일 레이아웃 생성
        val taskLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(8, 8, 8, 8)
            gravity = android.view.Gravity.START // 왼쪽 정렬
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0) // 상단 마진을 추가하여 공간 확보
            }
        }

        // 체크 표시와 밑줄이 있는 TextView 생성 (기본값은 체크 표시)
        val symbolTextView = TextView(this).apply {
            text = "✓" // 기본 체크 심볼
            textSize = 24f
            setTypeface(null, Typeface.BOLD) // 굵게 설정
            setPadding(8, 16, 8, 8)
            setTextColor(resources.getColor(android.R.color.black, null))
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG // 밑줄 추가
            setOnClickListener {
                showSymbolSelectionDialog(this) // 클릭 시 심볼 선택 팝업 표시
            }
        }

        // 할 일 이름을 표시할 TextView 생성
        val taskTextView = TextView(this).apply {
            text = taskName
            textSize = 18f
            setPadding(8, 8, 8, 8)
            setTextColor(resources.getColor(android.R.color.black, null))
            isClickable = true // 텍스트뷰를 클릭 가능하게 설정
            isFocusable = true // 포커스 가능하게 설정
        }

        // 수정 기능을 위한 클릭 리스너 추가
        taskTextView.setOnClickListener {
            if (isEditMode) {
                showEditTaskDialog(taskTextView) // 편집 모드일 때만 수정 다이얼로그 표시
            }
        }

        // 레이아웃에 심볼 TextView와 할 일 이름 TextView 추가
        taskLayout.addView(symbolTextView)
        taskLayout.addView(taskTextView)

        // 편집 모드일 때는 할 일 추가 시 바로 삭제 버튼도 함께 추가
        if (isEditMode) {
            val deleteButton = Button(this).apply {
                text = "삭제"
                tag = "deleteButton"
                setOnClickListener {
                    taskContainer.removeView(taskLayout) // 할 일 삭제
                }
            }
            taskLayout.addView(deleteButton, 0) // 왼쪽에 추가
        }

        taskContainer.addView(taskLayout) // 전체 할 일 목록에 추가

        // 현재 날짜에 할 일을 추가

    }

    // 편집 모드 전환 함수
    private fun toggleEditMode() {
        isEditMode = !isEditMode

        // 편집 모드 상태에 따라 텍스트 변경
        if (isEditMode) {
            editButton.text = "완료"
        } else {
            editButton.text = "편집"
        }

        // taskContainer의 자식 뷰들을 반복하며 각 항목에 삭제 버튼 추가 또는 제거
        for (i in 0 until taskContainer.childCount) {
            val taskLayout = taskContainer.getChildAt(i) as LinearLayout
            val deleteButton = taskLayout.findViewWithTag<Button>("deleteButton")

            if (isEditMode) {
                // 편집 모드일 때 삭제 버튼이 없으면 추가
                if (deleteButton == null) {
                    val deleteButton = Button(this).apply {
                        text = "삭제"
                        tag = "deleteButton"
                        setOnClickListener {
                            taskContainer.removeView(taskLayout) // 할 일 삭제
                        }
                    }
                    taskLayout.addView(deleteButton, 0) // 왼쪽에 추가
                }
            } else {
                // 편집 모드가 아닐 때는 삭제 버튼을 숨김
                deleteButton?.let {
                    taskLayout.removeView(it)
                }
            }
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val taskEditText = dialogView.findViewById<EditText>(R.id.editTaskName)

        // 선택된 날짜가 비어있지 않은지 확인
        if (selectedDate.isBlank()) {
            // 선택된 날짜가 없는 경우 현재 날짜를 사용
            selectedDate = "${Calendar.getInstance().get(Calendar.YEAR)}-${Calendar.getInstance().get(Calendar.MONTH) + 1}-${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)}"
        }

        // AlertDialog 생성 및 설정
        AlertDialog.Builder(this)
            .setTitle("새 할 일 추가")
            .setView(dialogView) // 커스텀 레이아웃 설정
            .setPositiveButton("확인") { dialog, _ ->
                val taskName = taskEditText.text.toString()
                if (taskName.isNotBlank()) {
                    // 선택된 날짜에 새 할 일을 추가
                    tasksMap.getOrPut(selectedDate) { mutableListOf() }.add(taskName)
                    addNewTask(taskName) // 입력된 제목으로 할 일 추가
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showEditTaskDialog(taskTextView: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val taskEditText = dialogView.findViewById<EditText>(R.id.editTaskName)
        taskEditText.setText(taskTextView.text) // 기존 할 일 제목을 다이얼로그에 표시

        // AlertDialog 생성 및 설정
        AlertDialog.Builder(this)
            .setTitle("할 일 수정")
            .setView(dialogView)
            .setPositiveButton("확인") { dialog, _ ->
                val updatedTaskName = taskEditText.text.toString()
                if (updatedTaskName.isNotBlank()) {
                    taskTextView.text = updatedTaskName // 할 일 이름 업데이트
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showSymbolSelectionDialog(symbolTextView: TextView) {
        val symbols = arrayOf("⬤", "▲", "✖") // 동그라미, 세모, 엑스
        AlertDialog.Builder(this)
            .setTitle("상태 선택")
            .setItems(symbols) { _, which ->
                // 선택한 심볼을 TextView에 반영하고 밑줄 제거
                symbolTextView.text = symbols[which]
                symbolTextView.paintFlags = 0 // 밑줄 제거
            }
            .show()
    }
}