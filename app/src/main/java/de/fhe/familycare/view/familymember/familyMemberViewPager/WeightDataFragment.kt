package de.fhe.familycare.view.familymember.familyMemberViewPager

import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.fhe.familycare.R
import de.fhe.familycare.databinding.FragmentWeightDataBinding
import de.fhe.familycare.storage.model.FamilyMember
import de.fhe.familycare.storage.model.WeightData
import de.fhe.familycare.view.core.BaseFragment
import de.fhe.familycare.view.familymember.FamilyMemberViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

/**
 * Fragment for WeightDataView
 */
class WeightDataFragment : BaseFragment() {

    companion object {
        private const val FAMILY_MEMBER_ID = "familyMemberId"
        private var timeframe:Timeframe = Timeframe.DAILY
        private var timePattern:DateTimeFormatter = DateTimeFormatter.ofPattern("EE")

        @JvmStatic
        fun newInstance(familyMemberId: Long) =
            WeightDataFragment().apply {
                arguments = Bundle().apply {
                    putLong(FAMILY_MEMBER_ID, familyMemberId)
                }
            }
    }

    private var _binding: FragmentWeightDataBinding? = null
    private  val binding get() = _binding!!
    private lateinit var viewModel : FamilyMemberViewModel
    private var familyMemberId: Long = 1
    private var weightDataSet = mutableListOf<WeightData>()
    private val weightEntriesList = mutableListOf<Entry>()
    private var lastWeightData: WeightData? = null
    private lateinit var familyMember: FamilyMember


    /**
     * sets familyMemberId
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("FM", "test")
        arguments?.let {
            familyMemberId = it.getLong(FAMILY_MEMBER_ID)
        }
    }

    /**
     * initializes ViewModel, observes FamilyMember and WeightData LiveData
     * sets OnClickListener to AddWeightData Button
     * @return binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Bind and Inflate the layout for this fragment
        _binding = FragmentWeightDataBinding.inflate(inflater, container, false)
        viewModel = this.getViewModel(FamilyMemberViewModel::class.java)

        viewModel.getFamilyMember(familyMemberId).observe(this.viewLifecycleOwner){
            familyMember = it
        }

        viewModel.getLastWeightDataByFamilyMemberId(familyMemberId).observe(this.viewLifecycleOwner){
            lastWeightData = it
        }

        binding.btnAddRecentWeightData?.setOnClickListener{
            Log.i("FM", "called weightDataDialog() with FM:${familyMember.name}")
            weightInputDialog()
        }
        return binding.root
    }

    /**
     * displays WeightData Input-Dialog
     */
    private fun weightInputDialog(){

        // edittext to get float number and set keyboard for float input
        val editText = EditText(requireContext())
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_weight_input, familyMember.name))
            .setView(editText)
            .setNegativeButton(getText(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }

        if(lastWeightData != null && dateIsTodaysDate(lastWeightData?.date!!)){

            // update existing weightdata
            builder.setMessage(getText(R.string.warning_weight_exists))
            builder.setPositiveButton(getString(R.string.save)){ dialog, _ ->
                if(!editText.text.isNullOrBlank()){
                    lastWeightData?.weight = editText.text.toString().toFloat()
                    lastWeightData?.age = viewModel.getAge(familyMember)
                    lastWeightData?.height = familyMember.height.toFloat()/100
                    viewModel.saveNewWeightData(lastWeightData!!)
                    dialog.dismiss()
                } else {
                    Snackbar.make(binding.root, getText(R.string.warning_weight_not_saved), Snackbar.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        } else {
            //save new weightdata
            builder.setMessage(getText(R.string.new_date_for_today))
            builder.setPositiveButton(getText(R.string.save)){ dialog, _ ->
                if(!editText.text.isNullOrBlank()){
                    val weightData = WeightData()
                    weightData.weight = editText.text.toString().toFloat()
                    weightData.date =  Date(System.currentTimeMillis())
                    weightData.familyMemberId = familyMember.id
                    weightData.height = familyMember.height.toFloat()/100
                    weightData.age = viewModel.getAge(familyMember)
                    viewModel.saveNewWeightData(weightData)
                    dialog.dismiss()
                } else {
                    Snackbar.make(binding.root, getText(R.string.warning_weight_not_saved), Snackbar.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        builder.show()
    }

    /**
     * check if a given date is today
     */
    private fun dateIsTodaysDate(inputDate: Date): Boolean{
        // formatter
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

        //today
        val today = Date(System.currentTimeMillis())

        // format dates to get date without time difference
        val dateInDays = sdf.format(inputDate)
        val todayInDays = sdf.format(today)

        Log.i("FM", "dateInDays: $dateInDays | todayInDays: $todayInDays")
        return dateInDays == todayInDays
    }

    /**
     * observes AllWeightData LiveData
     * initializes BMI View
     * sets OptionsMenu
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("FM", "$familyMemberId")

        initBMIView()

        viewModel.getAllWeightDataForFamilyMember(familyMemberId).observe(this.viewLifecycleOwner)
        {   weightDataList -> Log.i("FM", "${weightDataList.size}")
            weightEntriesList.clear()
            weightDataSet = weightDataList as MutableList<WeightData>
            refreshGraph()
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Refreshes WeightData Graph if another interval is selected via the OptionsMenu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.action_weightdata_daily ->{
                timeframe = Timeframe.DAILY
                refreshGraph()
                return true
            }
            R.id.action_weightdata_weekly->{
                timeframe = Timeframe.WEEKLY
                refreshGraph()
                return true
            }
            R.id.action_weightdata_monthly ->{
                timeframe = Timeframe.MONTHLY
                refreshGraph()
                return true
            }
            R.id.action_weightdata_yearly ->{
                timeframe = Timeframe.YEARLY
                refreshGraph()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * refreshes the WeightDataGraph, switches between Portrait and Landscape View,
     * displays all WeightData in the interval
     */
    private fun refreshGraph(){
        weightEntriesList.clear()
        var lastTimeStamp = 0

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            when (timeframe) {

                Timeframe.DAILY -> {
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusDays(6)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusDays(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.get(
                            WeekFields.of(Locale.getDefault()).dayOfWeek()
                        )
                        //takes only the first value of each day and limit it by the value range
                        if (weightDate != lastTimeStamp && weightData.date!!.time > minXAxis) {
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(
                                Entry(weightData.date!!.time.toFloat(), weightData.weight)
                            )
                        }
                    }

                    timePattern = DateTimeFormatter.ofPattern("EE")
                    styleGraph(minXAxis, maxXAxis, 7)
                }
                Timeframe.WEEKLY -> {
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusWeeks(3)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusWeeks(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.get(
                            WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
                        )
                        //takes only the first value of each week and limit it by the value range
                        if (weightDate != lastTimeStamp && weightData.date!!.time > minXAxis) {
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(
                                Entry(
                                    weightData.date!!.time.toFloat(),
                                    weightData.weight
                                )
                            )
                        }
                    }

                    timePattern = DateTimeFormatter.ofPattern("w")
                    styleGraph(minXAxis, maxXAxis, 4)
                }
                Timeframe.MONTHLY -> {
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusMonths(11)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusMonths(0)).toFloat()
                    val currentMonth = LocalDateTime.now().minusMonths(0).monthValue

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.monthValue

                        //takes only the first value of each second Month and limit it by the value range
                        if (weightDate != lastTimeStamp && currentMonth.mod(2) == weightDate!!.mod(
                                2) && weightData.date!!.time > minXAxis) {
                            lastTimeStamp = weightDate
                            weightEntriesList.add(
                                Entry(
                                    weightData.date!!.time.toFloat(),
                                    weightData.weight
                                )
                            )
                        }
                    }

                    timePattern = DateTimeFormatter.ofPattern("LLL")
                    styleGraph(minXAxis, maxXAxis, 6)
                }
                Timeframe.YEARLY -> {
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusYears(4)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusYears(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.year

                        //takes only the first value of each year and limit it by the value range
                        if (weightDate != lastTimeStamp && weightData.date!!.time > minXAxis) {
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(
                                Entry(
                                    weightData.date!!.time.toFloat(),
                                    weightData.weight
                                )
                            )
                        }
                    }
                    timePattern = DateTimeFormatter.ofPattern("uu")
                    styleGraph(minXAxis, maxXAxis, 5)
                }
            }
        }
        //landscape mode
        else{
            when(timeframe){

                Timeframe.DAILY ->{
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusDays(13)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusDays(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.
                        get(WeekFields.of(Locale.getDefault()).dayOfWeek())

                        //takes only the first value of each day and limit it by the value range
                        if (weightDate != lastTimeStamp && weightData.date!!.time > minXAxis) {
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(Entry(weightData.date!!.time.toFloat(), weightData.weight)
                            )
                        }
                    }

                    timePattern = DateTimeFormatter.ofPattern("EE")
                    styleGraph(minXAxis, maxXAxis, 14)
                }
                Timeframe.WEEKLY->{
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusWeeks(7)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusWeeks(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.
                        get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())

                        //takes only the first value of each week and limit it by the value range
                        if (weightDate != lastTimeStamp && weightData.date!!.time > minXAxis){
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(Entry(weightData.date!!.time.toFloat(), weightData.weight))
                        }
                    }

                    timePattern = DateTimeFormatter.ofPattern("w")
                    styleGraph(minXAxis, maxXAxis, 8)
                }
                Timeframe.MONTHLY ->{
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusMonths(11)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusMonths(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.monthValue

                        //takes only the first value of each month and limit it by the value range
                        if (weightDate != lastTimeStamp &&
                            weightData.date!!.time > minXAxis
                        ){
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(Entry(weightData.date!!.time.toFloat(), weightData.weight))
                        }
                    }

                    timePattern = DateTimeFormatter.ofPattern("LLL")
                    styleGraph(minXAxis, maxXAxis, 12)
                }
                Timeframe.YEARLY ->{
                    val minXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusYears(9)).toFloat()
                    val maxXAxis =
                        convertLocalDateTimeToMilli(LocalDateTime.now().minusYears(0)).toFloat()

                    weightDataSet.forEachIndexed { _, weightData ->
                        val weightDate = convertDateToLocalDate(weightData.date)?.year

                        //takes only the first value of each year and limit it by the value range
                        if (weightDate != lastTimeStamp && weightData.date!!.time > minXAxis){
                            lastTimeStamp = weightDate!!
                            weightEntriesList.add(Entry(weightData.date!!.time.toFloat(), weightData.weight))
                        }
                    }
                    timePattern = DateTimeFormatter.ofPattern("uu")
                    styleGraph(minXAxis, maxXAxis, 10)
                }
            }
        }
        loadNewDataset()
    }


    private fun convertDateToLocalDate(date: Date?): LocalDate? {
        return date!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
    private fun convertLocalDateTimeToMilli(localDateTime: LocalDateTime):Long{
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun loadNewDataset(){
        val lineChart = binding.lcWightChart

        val weightEntriesLineDataSet = LineDataSet(weightEntriesList, "Label")
        weightEntriesLineDataSet.valueFormatter = TimeXAxisFormatter()
        val lineData = LineData(weightEntriesLineDataSet)
        styleLineDataSet(weightEntriesLineDataSet)

        lineChart.data = lineData
        lineChart.data.setValueFormatter(TimeXAxisFormatter())
    }

    /**
     * Styles the WeightData graph
     */
    private fun styleGraph(minXAxis:Float, maxXAxis:Float, xAxisLabelCount:Int): LineChart {
        val lineChart:LineChart = binding.lcWightChart

        lineChart.apply {
            axisRight.isEnabled = false
            axisLeft.isEnabled = false

            xAxis.apply {
                axisMinimum = minXAxis
                axisMaximum = maxXAxis
                setLabelCount(xAxisLabelCount, true)
                setDrawGridLines(false)
                isGranularityEnabled = true
                position = XAxis.XAxisPosition.BOTTOM_INSIDE
                setDrawAxisLine(false)
                textSize = 20F
                valueFormatter = TimeXAxisFormatter()

            }

            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(true)
            setPinchZoom(false)

            description = null
            legend.isEnabled = false
            setNoDataText(getString(R.string.no_data))

            extraLeftOffset = 20F
            extraRightOffset = 20F
            extraBottomOffset = 10F
            extraTopOffset = 10F

            notifyDataSetChanged()
            invalidate()

        }
        return lineChart
    }

    /**
     * Styles LineDataSet
     * @param lineDataSet: the LineDataSet to be styled
     * @return the styled LineDataSet
     */
    private fun styleLineDataSet(lineDataSet: LineDataSet): LineDataSet {
        lineDataSet.apply {
            color = resources.getColor(R.color.primaryLightColor, requireContext().theme)
            setCircleColor(resources.getColor(R.color.primaryColor, requireContext().theme))
            circleRadius = 10F
            circleHoleRadius = 5F
            lineWidth = 7.5F
            valueTextSize = 20F
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }
        return lineDataSet
    }

    /**
     * formats the values of the x-axis of the WeightDataGraph
     */
    private class TimeXAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String{

            val localTime: LocalDate = Instant.ofEpochMilli(value.toLong()).atZone(ZoneId.systemDefault()).toLocalDate()
            if (timeframe == Timeframe.WEEKLY){
                return "KW" + timePattern.format(localTime)
            }
            return timePattern.format(localTime)
        }
    }

    private enum class Timeframe{
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    /**
     * observes the FamilyMember and displays the bmi and what it means if the FamilyMember is human
     * and at least 18 years old
     */
    private fun initBMIView(){
        viewModel.getFamilyMember(familyMemberId).observe(this.viewLifecycleOwner){
            if(it.isHuman){
                viewModel.getLastWeightDataByFamilyMemberId(familyMemberId).observe(this.viewLifecycleOwner){weightData ->
                    if(weightData != null){
                        if(weightData.age>=18 && weightData.height > 0){
                            binding.tvBMI?.visibility = View.VISIBLE
                            val bmi = weightData.weight / (weightData.height*2)
                            val status: String = when {
                                bmi < 18.5 -> {
                                    getString(R.string.underweight)
                                }
                                bmi < 25 -> {
                                    getString(R.string.normal_weight)
                                }
                                bmi < 30 -> {
                                    getString(R.string.a_little_too_much)
                                }
                                bmi < 35 -> {
                                    getString(R.string.obesity)
                                }
                                bmi < 40 -> {
                                    getString(R.string.more_obesity)
                                }
                                else -> {
                                    getString(R.string.extreme_obesity)
                                }
                            }

                            val roundedBmi = BigDecimal(bmi.toDouble()).setScale(2, RoundingMode.HALF_EVEN)
                            binding.tvBMI?.text = getString(R.string.bmi_formatted, roundedBmi.toString(), it.name, status)
                        }else{
                            binding.tvBMI?.visibility = View.VISIBLE
                            binding.tvBMI?.text = getText(R.string.no_bmi_under_18)
                        }
                    }
                }
            }else{
                binding.tvBMI?.visibility = View.GONE
            }
        }

    }
}

