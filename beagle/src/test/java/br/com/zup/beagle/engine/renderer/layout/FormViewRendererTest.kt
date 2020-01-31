package br.com.zup.beagle.engine.renderer.layout

import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.zup.beagle.action.ActionExecutor
import br.com.zup.beagle.action.FormValidationActionHandler
import br.com.zup.beagle.engine.renderer.RootView
import br.com.zup.beagle.engine.renderer.ViewRenderer
import br.com.zup.beagle.engine.renderer.ViewRendererFactory
import br.com.zup.beagle.extensions.once
import br.com.zup.beagle.form.FormResult
import br.com.zup.beagle.form.FormSubmitter
import br.com.zup.beagle.form.ValidatorHandler
import br.com.zup.beagle.form.Validator
import br.com.zup.beagle.form.FormValidatorController
import br.com.zup.beagle.logger.BeagleLogger
import br.com.zup.beagle.logger.BeagleMessageLogs
import br.com.zup.beagle.mockdata.FormInputView
import br.com.zup.beagle.mockdata.FormInputViewWithoutValidation
import br.com.zup.beagle.testutil.RandomData
import br.com.zup.beagle.testutil.getPrivateField
import br.com.zup.beagle.utils.hideKeyboard
import br.com.zup.beagle.view.ViewFactory
import br.com.zup.beagle.widget.form.Form
import br.com.zup.beagle.widget.form.FormInput
import br.com.zup.beagle.widget.form.FormSubmit
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val FORM_INPUT_VIEWS_FIELD_NAME = "formInputViews"
private const val FORM_SUBMIT_VIEW_FIELD_NAME = "formSubmitView"
private val INPUT_VALUE = RandomData.string()

class FormViewRendererTest {

    @MockK
    private lateinit var form: Form
    @MockK(relaxed = true)
    private lateinit var formInput: FormInput
    @MockK
    private lateinit var formSubmit: FormSubmit
    @MockK
    private lateinit var viewRendererFactory: ViewRendererFactory
    @MockK
    private lateinit var validatorHandler: ValidatorHandler
    @MockK
    private lateinit var validator: Validator<Any, Any>
    @MockK
    private lateinit var formValidationActionHandler: FormValidationActionHandler
    @MockK(relaxed = true)
    private lateinit var formValidatorController: FormValidatorController
    @MockK(relaxed = true)
    private lateinit var actionExecutor: ActionExecutor
    @MockK
    private lateinit var formSubmitter: FormSubmitter
    @MockK
    private lateinit var viewRenderer: ViewRenderer<*>
    @MockK
    private lateinit var viewFactory: ViewFactory
    @MockK
    private lateinit var appCompatActivity: AppCompatActivity
    @MockK
    private lateinit var inputMethodManager: InputMethodManager
    @MockK(relaxed = true)
    private lateinit var formInputView: FormInputView
    @MockK(relaxed = true)
    private lateinit var formInputViewWithoutValidation: FormInputViewWithoutValidation
    @MockK
    private lateinit var formSubmitView: View
    @MockK
    private lateinit var viewGroup: ViewGroup
    @MockK
    private lateinit var rootView: RootView

    private val onClickListenerSlot = slot<View.OnClickListener>()
    private val formResultCallbackSlot = slot<(formResult: FormResult) -> Unit>()
    private val runnableSlot = slot<Runnable>()

    private lateinit var formViewRenderer: FormViewRenderer

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        formViewRenderer = FormViewRenderer(
            form,
            validatorHandler,
            formValidationActionHandler,
            formSubmitter,
            formValidatorController,
            actionExecutor,
            viewRendererFactory,
            viewFactory
        )

        mockkStatic("br.com.zup.beagle.utils.ViewExtensionsKt")
        mockkObject(BeagleMessageLogs)

        every { BeagleMessageLogs.logFormInputsNotFound(any()) } just Runs
        every { BeagleMessageLogs.logFormSubmitNotFound(any()) } just Runs
        every { viewRendererFactory.make(form) } returns viewRenderer
        every { viewRenderer.build(rootView) } returns viewGroup
        every { form.child } returns form
        every { form.action } returns RandomData.string()
        every { formInput.required } returns false
        every { formInputViewWithoutValidation.hideKeyboard() } just Runs
        every { formInputViewWithoutValidation.context } returns appCompatActivity
        every { formInputViewWithoutValidation.tag } returns formInput
        every { formInputView.hideKeyboard() } just Runs
        every { formInputView.getValue() } returns INPUT_VALUE
        every { formInputView.context } returns appCompatActivity
        every { formInputView.tag } returns formInput
        every { formSubmitView.tag } returns formSubmit
        every { formSubmitView.context } returns appCompatActivity
        every { formSubmitView.setOnClickListener(capture(onClickListenerSlot)) } just Runs
        every { viewGroup.childCount } returns 2
        every { viewGroup.getChildAt(0) } returns formInputView
        every { viewGroup.getChildAt(1) } returns formSubmitView
        every { formValidationActionHandler.formInputViews = any() } just Runs
        every { appCompatActivity.getSystemService(any()) } returns inputMethodManager
        every { appCompatActivity.runOnUiThread(capture(runnableSlot)) } just Runs
        every { formSubmitter.submitForm(any(), any(), capture(formResultCallbackSlot)) } just Runs
        every { validatorHandler.getValidator(any()) } returns validator
    }

    @After
    fun tearDown() {
        unmockkStatic("br.com.zup.beagle.utils.ViewExtensionsKt")
        unmockkObject(BeagleLogger)
        unmockkObject(BeagleMessageLogs)
    }

    @Test
    fun build_should_not_try_to_iterate_over_children_if_is_not_a_ViewGroup() {
        // Given
        every { viewGroup.childCount } returns 0
        every { viewRenderer.build(rootView) } returns formInputView

        // When
        val actual = formViewRenderer.build(rootView)

        // Then
        assertEquals(formInputView, actual)
        verify(exactly = 0) { viewGroup.childCount }
    }

    @Test
    fun build_should_try_to_iterate_over_all_viewGroups() {
        // Given
        val childViewGroup = mockk<ViewGroup>()
        every { childViewGroup.childCount } returns 0
        every { childViewGroup.tag } returns null
        every { viewGroup.childCount } returns 1
        every { viewGroup.getChildAt(any()) } returns childViewGroup

        // When
        formViewRenderer.build(rootView)

        // Then
        verify(exactly = 1) { childViewGroup.childCount }
    }

    @Test
    fun build_should_try_to_iterate_over_all_viewGroups_that_is_the_formInput() {
        // Given
        val childViewGroup = mockk<ViewGroup>()
        every { childViewGroup.childCount } returns 0
        every { childViewGroup.tag } returns mockk<FormInput>()
        every { viewGroup.childCount } returns 1
        every { viewGroup.getChildAt(any()) } returns childViewGroup

        // When
        formViewRenderer.build(rootView)

        // Then
        val views = formViewRenderer.getPrivateField<List<View>>(FORM_INPUT_VIEWS_FIELD_NAME)
        assertEquals(1, views.size)
    }

    @Test
    fun build_should_group_formInput_views() {
        formViewRenderer.build(rootView)

        val views = formViewRenderer.getPrivateField<List<View>>(FORM_INPUT_VIEWS_FIELD_NAME)
        assertEquals(1, views.size)
        assertEquals(formInputView, views[0])
        verify { formValidatorController.formSubmitView = formSubmitView }
    }

    @Test
    fun build_should_find_formSubmitView() {
        formViewRenderer.build(rootView)

        val actual = formViewRenderer.getPrivateField<View>(FORM_SUBMIT_VIEW_FIELD_NAME)
        assertEquals(formSubmitView, actual)
        verify(exactly = once()) { formSubmitView.setOnClickListener(any()) }
        verify { formValidatorController.configFormSubmit() }
    }

    @Test
    fun build_should_call_configFormSubmit_on_fetchForms() {
        formViewRenderer.build(rootView)

        verify { formValidatorController.configFormSubmit() }
    }

    @Test
    fun onClick_of_formSubmit_should_set_formInputViews_on_formValidationActionHandler() {
        // Given When
        executeFormSubmitOnClickListener()

        // Then
        val views = formViewRenderer.getPrivateField<List<View>>(FORM_INPUT_VIEWS_FIELD_NAME)
        verify(exactly = once()) { formValidationActionHandler.formInputViews = views }
    }

    @Test
    fun onClick_of_formSubmit_should_set_submit_form_when_inputValue_is_not_required() {
        // Given When
        executeFormSubmitOnClickListener()

        // Then
        verify(exactly = once()) { formInputView.hideKeyboard() }
        verify(exactly = once()) { formSubmitter.submitForm(any(), any(), any()) }
    }

    @Test
    fun onClick_of_formSubmit_should_validate_formField_that_is_required_and_is_valid() {
        // Given
        every { formInput.required } returns true
        every { validator.isValid(any(), any()) } returns true

        // When
        executeFormSubmitOnClickListener()

        // Then
        verify(exactly = once()) { validator.isValid(INPUT_VALUE, any()) }
        verify(exactly = once()) { formSubmitter.submitForm(any(), any(), any()) }
    }

    @Test
    fun onClick_of_formSubmit_should_validate_formField_that_is_required_and_that_not_is_valid() {
        // Given
        every { formInput.required } returns true
        every { validator.isValid(any(), any()) } returns false

        // When
        executeFormSubmitOnClickListener()

        // Then
        verify(exactly = once()) { formInputView.onValidationError(any()) }
        verify(exactly = 0) { formSubmitter.submitForm(any(), any(), any()) }
    }

    @Test
    fun onClick_of_formSubmit_should_validate_formField_that_does_implement_validation() {
        // Given
        every { formInput.required } returns true
        every { validator.isValid(any(), any()) } returns false
        every { viewGroup.getChildAt(0) } returns formInputViewWithoutValidation
        every { BeagleMessageLogs.logInvalidFormInputState(any()) } just Runs

        // When
        executeFormSubmitOnClickListener()

        // Then
        verify(exactly = once()) { BeagleMessageLogs.logInvalidFormInputState(formInput.name) }
        verify(exactly = 0) { formSubmitter.submitForm(any(), any(), any()) }
    }

    @Test
    fun onClick_of_formSubmit_should_handleFormSubmit_and_call_actionExecutor() {
        // Given
        val formResult = FormResult.Success(mockk())

        // When
        executeFormSubmitOnClickListener()
        formResultCallbackSlot.captured(formResult)
        runnableSlot.captured.run()

        // Then
        verify(exactly = once()) { actionExecutor.doAction(appCompatActivity, formResult.action) }
    }

    @Test
    fun onClick_of_formSubmit_should_handleFormSubmit_and_call_showError() {
        // Given
        val formResult = FormResult.Error(mockk())
        val alertDialogBuilder = mockk<AlertDialog.Builder>()
        every { alertDialogBuilder.setTitle(any<String>()) } returns alertDialogBuilder
        every { alertDialogBuilder.setMessage(any<String>()) } returns alertDialogBuilder
        every {
            alertDialogBuilder.setPositiveButton(
                any<String>(),
                any()
            )
        } returns alertDialogBuilder
        every { alertDialogBuilder.show() } returns mockk()
        every { viewFactory.makeAlertDialogBuilder(appCompatActivity) } returns alertDialogBuilder


        // When
        executeFormSubmitOnClickListener()
        formResultCallbackSlot.captured(formResult)
        runnableSlot.captured.run()

        // Then
        verify(exactly = once()) { alertDialogBuilder.setTitle("Error!") }
        verify(exactly = once()) { alertDialogBuilder.setMessage("Something went wrong!") }
        verify(exactly = once()) { alertDialogBuilder.show() }
    }

    private fun executeFormSubmitOnClickListener() {
        formViewRenderer.build(rootView)
        onClickListenerSlot.captured.onClick(formSubmitView)
    }
}