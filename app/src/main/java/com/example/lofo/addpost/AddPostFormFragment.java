package com.example.lofo.addpost;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.lofo.R;
import com.example.lofo.model.ItemPost;
import com.example.lofo.util.ToastUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.Field;
import java.util.Calendar;

public class AddPostFormFragment extends Fragment {

    public static final String ARG_POST_TYPE = "postType";

    private boolean isScrolling = false;

    private static final String[] CATEGORIES = {
            // Electronics
            "Mobile Phone", "Laptop / Tablet", "Headphones / Earbuds",
            "Charger / Power Bank", "USB Drive / Hard Disk", "Camera",

            // Valuables
            "Wallet / Purse", "Cash", "Debit / Credit Card",
            "Jewellery",

            // Identity & Documents
            "ID Card", "Passport",
            "Documents", "Vehicle Documents", "Tickets",

            // Bags
            "Bag / Backpack", "Travel Bag / Luggage",

            // Daily Use
            "Keys", "Clothing", "Shoes / Footwear",
            "Watch", "Glasses / Sunglasses",
            "Umbrella", "Water Bottle", "Lunch Box / Tiffin",

            // Academic / Office
            "Books", "Notebook", "Stationery", "Calculator",

            // Personal / Others
            "Makeup / Cosmetics", "Medical Items",
            "Pet", "Pet Accessories",
            "Sports Equipment", "Toys", "Gift Item",

            // Fallback
            "Other"
    };

    private static final String[] MONTHS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private String postType;
    private AddPostViewModel viewModel;
    private Uri selectedImageUri = null;

    // ── Track whether user has changed the date from today (for validation) ──
    private boolean dateChanged = false;

    private TextInputLayout tilItemName, tilCategory, tilLocation, tilDescription,
            tilContactEmail, tilContactNumber;
    private TextInputEditText etItemName, etLocation, etDescription,
            etContactEmail, etContactNumber;
    private MaterialAutoCompleteTextView actvCategory;
    private NumberPicker pickerDay, pickerMonth, pickerYear;
    private MaterialCardView cardDatePicker;
    private TextView tvDateLabel, tvDateRequired;
    private MaterialButton btnSubmit;
    private CircularProgressIndicator progressSubmit;
    private TextView tvFormTitle, tvFormError;
    private FrameLayout frameImagePicker;
    private LinearLayout layoutImagePlaceholder;
    private ImageView ivImagePreview, ivRemoveImage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            selectedImageUri = result.getData().getData();
                            viewModel.setSelectedImageUri(selectedImageUri);
                            showImagePreview(selectedImageUri);
                        }
                    });

    public static AddPostFormFragment newInstance(String postType) {
        AddPostFormFragment f = new AddPostFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST_TYPE, postType);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_post_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bottom padding so submit button clears the bottom nav bar
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.formScrollView),
                (v, insets) -> {
                    int statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                    int navBar    = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    int bottomPad = navBar + dpToPx(50);
                    v.setPadding(v.getPaddingLeft(), statusBar, v.getPaddingRight(), bottomPad);
                    return WindowInsetsCompat.CONSUMED;
                });

        postType = getArguments() != null
                ? getArguments().getString(ARG_POST_TYPE, "lost") : "lost";

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        requireActivity().getApplication()))
                .get(AddPostViewModel.class);

        bindViews(view);
        applyPostTypeTheme();
        setupCategoryDropdown();
        setupDatePickers();
        setupImagePicker();
        setupFocusClearers();
        autoFillUserDetails();

        view.findViewById(R.id.ivBack).setOnClickListener(v -> popBack());
        btnSubmit.setOnClickListener(v -> attemptSubmit());
        observeViewModel();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void applyPostTypeTheme() {

        if ("found".equals(postType)) {

            tvFormTitle.setText(getString(R.string.title_report_found));
            btnSubmit.setText(getString(R.string.btn_post_found));

            // Dynamic hints
            tilLocation.setHint(getString(R.string.hint_location_found));
            tvDateLabel.setText(getString(R.string.hint_date_found));

        } else {

            tvFormTitle.setText(getString(R.string.title_report_lost));
            btnSubmit.setText(getString(R.string.btn_post_lost));

            // Dynamic hints
            tilLocation.setHint(getString(R.string.hint_location_lost));
            tvDateLabel.setText(getString(R.string.hint_date_lost));
        }
    }

    // ─── Bind ─────────────────────────────────────────────────────────────────

    private void bindViews(View view) {
        tvFormTitle            = view.findViewById(R.id.tvFormTitle);
        tilItemName            = view.findViewById(R.id.tilItemName);
        tilCategory            = view.findViewById(R.id.tilCategory);
        tilLocation            = view.findViewById(R.id.tilLocation);
        tilDescription         = view.findViewById(R.id.tilDescription);
        tilContactEmail        = view.findViewById(R.id.tilContactEmail);
        tilContactNumber       = view.findViewById(R.id.tilContactNumber);
        etItemName             = view.findViewById(R.id.etItemName);
        actvCategory           = view.findViewById(R.id.actvCategory);
        etLocation             = view.findViewById(R.id.etLocation);
        etDescription          = view.findViewById(R.id.etDescription);
        etContactEmail         = view.findViewById(R.id.etContactEmail);
        etContactNumber        = view.findViewById(R.id.etContactNumber);
        pickerDay              = view.findViewById(R.id.pickerDay);
        pickerMonth            = view.findViewById(R.id.pickerMonth);
        pickerYear             = view.findViewById(R.id.pickerYear);
        cardDatePicker         = view.findViewById(R.id.cardDatePicker);
        tvDateLabel            = view.findViewById(R.id.tvDateLabel);
        tvDateRequired         = view.findViewById(R.id.tvDateRequired);
        btnSubmit              = view.findViewById(R.id.btnSubmit);
        progressSubmit         = view.findViewById(R.id.progressSubmit);
        tvFormError            = view.findViewById(R.id.tvFormError);
        frameImagePicker       = view.findViewById(R.id.frameImagePicker);
        layoutImagePlaceholder = view.findViewById(R.id.layoutImagePlaceholder);
        ivImagePreview         = view.findViewById(R.id.ivImagePreview);
        ivRemoveImage          = view.findViewById(R.id.ivRemoveImage);
    }

    // ─── Category dropdown with accent blink on selection ────────────────────

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                R.layout.item_category_dropdown,
                CATEGORIES) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults r = new FilterResults();
                        r.values = CATEGORIES;
                        r.count  = CATEGORIES.length;
                        return r;
                    }
                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            tilCategory.setError(null);
        });

        actvCategory.setAdapter(adapter);
        actvCategory.setDropDownBackgroundResource(R.drawable.bg_dropdown);
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
    }

    // ─── NumberPicker - Date Wheels ─────────────────────────────────────────────────────────

    private void setupDatePickers() {
        Calendar cal = Calendar.getInstance();
        int currentDay   = cal.get(Calendar.DAY_OF_MONTH);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear  = cal.get(Calendar.YEAR);

        // Configure ranges and defaults
        pickerDay.setMinValue(1);
        pickerDay.setMaxValue(31);
        pickerDay.setValue(currentDay);
        pickerDay.setWrapSelectorWheel(true);

        pickerMonth.setMinValue(0);
        pickerMonth.setMaxValue(11);
        pickerMonth.setValue(currentMonth);
        pickerMonth.setDisplayedValues(MONTHS);
        pickerMonth.setWrapSelectorWheel(true);

        pickerYear.setMinValue(currentYear - 2);
        pickerYear.setMaxValue(currentYear);
        pickerYear.setValue(currentYear);
        pickerYear.setWrapSelectorWheel(false);

        // ── Style AFTER layout so children exist ──────────────────────────────
        // post() delays until after the first measure/layout pass, guaranteeing
        // that NumberPicker has inflated its internal EditText child.
        pickerDay.post(()   -> styleNumberPicker(pickerDay));
        pickerMonth.post(() -> styleNumberPicker(pickerMonth));
        pickerYear.post(()  -> styleNumberPicker(pickerYear));

        // Re-style after each scroll so the center text stays white while spinning
        NumberPicker.OnScrollListener scrollListener = (picker, scrollState) -> {
            // Re-apply on every scroll state change
            picker.post(() -> styleNumberPicker(picker));
        };
        pickerDay.setOnScrollListener(scrollListener);
        pickerMonth.setOnScrollListener(scrollListener);
        pickerYear.setOnScrollListener(scrollListener);

        // Date defaults to today — no validation needed, just grab value at submit
    }

    /**
     * Styles a NumberPicker without requiring keyboard interaction:
     *
     * 1. EditText child (selected/center slot):
     *    Set directly → white text, Poppins font.
     *    This is the only child NumberPicker exposes via getChildAt().
     *
     * 2. Wheel (non-selected / dim slots):
     *    These are drawn by mSelectorWheelPaint — a private Paint field.
     *    We set its color to our dim color via reflection.
     *    We also set mTextColor and mSelectedTextColor for the same reason.
     *
     * 3. Divider lines:
     *    mSelectionDivider → accent color drawable, also via reflection.
     *
     * Called via post() to guarantee children are attached before access.
     */
    private void styleNumberPicker(NumberPicker picker) {
        if (!isAdded()) return; // guard if fragment detached

        int white   = requireContext().getColor(R.color.white);
        // Dim color for non-selected items — using text_hint from colors.xml
        int dimColor = requireContext().getColor(R.color.text_hint);
        int accent  = requireContext().getColor(R.color.color_accent);
        Typeface poppins = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);

        // ── 1. Style the EditText child (selected slot) ───────────────────────
        for (int i = 0; i < picker.getChildCount(); i++) {
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                EditText et = (EditText) child;
                et.setTextColor(white);
                et.setIncludeFontPadding(false);
                if (poppins != null) et.setTypeface(poppins);
                // Remove the default underline cursor line in the EditText
                et.setBackgroundColor(Color.TRANSPARENT);
                break;
            }
        }

        // ── 2. Wheel paint (dim items) via reflection ─────────────────────────
        try {
            // mSelectorWheelPaint draws ALL wheel items — we set it to dimColor
            Field paintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
            paintField.setAccessible(true);
            Paint paint = (Paint) paintField.get(picker);
            if (paint != null) {
                paint.setColor(dimColor);
                if (poppins != null) paint.setTypeface(poppins);
            }
        } catch (Exception ignored) {}

        try {
            // mTextColor — fallback field name on some Android versions
            Field f = NumberPicker.class.getDeclaredField("mTextColor");
            f.setAccessible(true);
            f.set(picker, dimColor);
        } catch (Exception ignored) {}

        try {
            // mSelectedTextColor — the center value override
            Field f = NumberPicker.class.getDeclaredField("mSelectedTextColor");
            f.setAccessible(true);
            f.set(picker, white);
        } catch (Exception ignored) {}

        // ── 3. Accent divider lines ───────────────────────────────────────────
        try {
            Field dividerField = NumberPicker.class.getDeclaredField("mSelectionDivider");
            dividerField.setAccessible(true);
            dividerField.set(picker,
                    new android.graphics.drawable.ColorDrawable(accent));
        } catch (Exception ignored) {}

        // Force redraw with new paint settings
        picker.invalidate();
    }

    private String getSelectedDate() {
        return String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d",
                pickerDay.getValue(), pickerMonth.getValue() + 1, pickerYear.getValue());
    }


    // ─── Date validation UI helpers ───────────────────────────────────────────

    private void showDateError(String message) {
        cardDatePicker.setCardBackgroundColor(
                requireContext().getColor(R.color.color_searchbar)
        );

        cardDatePicker.setStrokeColor(
                requireContext().getColor(R.color.color_error_red)
        );
        cardDatePicker.setStrokeWidth(dpToPx(1));

        tvDateRequired.setText(message);
        tvDateRequired.setTextColor(
                requireContext().getColor(R.color.color_error_red)
        );
    }

    private void resetDateError() {
        cardDatePicker.setCardBackgroundColor(
                requireContext().getColor(R.color.color_searchbar)
        );

        cardDatePicker.setStrokeWidth(0); // remove red border

        tvDateRequired.setText(getString(R.string.required_field));
        tvDateRequired.setTextColor(
                requireContext().getColor(R.color.text_hint_light)
        );
    }

    // ─── Auto-fill email + name (both read-only on the form) ─────────────────

    private void autoFillUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (user.getEmail() != null) {
            etContactEmail.setText(user.getEmail());
        }

        // Posted By field — auto-filled from Firebase display name
        TextInputEditText etPostedBy = requireView().findViewById(R.id.etPostedBy);
        if (etPostedBy != null) {
            String name = user.getDisplayName();
            etPostedBy.setText((name != null && !name.isEmpty()) ? name : "Anonymous");
        }
    }

    // ─── Image picker ─────────────────────────────────────────────────────────

    private void setupImagePicker() {
        frameImagePicker.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
        ivRemoveImage.setOnClickListener(v -> clearImage());
    }

    private void showImagePreview(Uri uri) {
        layoutImagePlaceholder.setVisibility(View.GONE);
        ivImagePreview.setVisibility(View.VISIBLE);
        ivRemoveImage.setVisibility(View.VISIBLE);
        // fitCenter so portrait images show fully without cropping
        Glide.with(this).load(uri).fitCenter().into(ivImagePreview);
    }

    private void clearImage() {
        selectedImageUri = null;
        viewModel.setSelectedImageUri(null);
        ivImagePreview.setVisibility(View.GONE);
        ivRemoveImage.setVisibility(View.GONE);
        layoutImagePlaceholder.setVisibility(View.VISIBLE);
    }

    private void setupFocusClearers() {
        etItemName.setOnFocusChangeListener((v, f)       -> { if (f) tilItemName.setError(null); });
        actvCategory.setOnFocusChangeListener((v, f)  -> { if (f) tilCategory.setError(null); });
        etLocation.setOnFocusChangeListener((v, f)    -> { if (f) tilLocation.setError(null); });
        etDescription.setOnFocusChangeListener((v, f) -> { if (f) tilDescription.setError(null); });
    }

    // ─── Validation + submit ──────────────────────────────────────────────────

    private void attemptSubmit() {
        tilItemName.setError(null);
        tilCategory.setError(null);
        tilLocation.setError(null);
        tilDescription.setError(null);
//        resetDateError();
        tvFormError.setVisibility(View.GONE);

        String title       = getText(etItemName);
        String category    = actvCategory.getText() != null
                ? actvCategory.getText().toString().trim() : "";
        String location    = getText(etLocation);
        String description = getText(etDescription);
        String phone       = getText(etContactNumber);
        String email       = getText(etContactEmail);
        String date        = getSelectedDate();

        boolean valid = true;
        if (TextUtils.isEmpty(title))       { tilItemName.setError(getString(R.string.error_item_name_required));        valid = false; }
        if (TextUtils.isEmpty(category))    { tilCategory.setError(getString(R.string.error_category_required));      valid = false; }
        if (TextUtils.isEmpty(location))    { tilLocation.setError(getString(R.string.error_location_required));      valid = false; }
        if (TextUtils.isEmpty(description)) { tilDescription.setError(getString(R.string.error_description_required)); valid = false; }

        // ── Date validation: user must scroll at least one picker ─────────────
//        if (!dateChanged) {
//            showDateError(getString(R.string.error_date_required));
//            valid = false;
//        }

        if (!valid) return;

        // Get Posted By name
        String postedBy = "";
        TextInputEditText etPostedBy = requireView().findViewById(R.id.etPostedBy);
        if (etPostedBy != null) postedBy = etPostedBy.getText() != null
                ? etPostedBy.getText().toString().trim() : "";

        ItemPost item = new ItemPost();
        item.setTitle(title);
        item.setCategory(category);
        item.setLocationName(location);
        item.setDescription(description);
        item.setStatus(postType);
        item.setDateLostOrFound(date);
        item.setContactEmail(email);
        item.setContactNumber(phone.isEmpty() ? null : phone);

        viewModel.submitPost(item, selectedImageUri);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    // ─── Observe ──────────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnSubmit.setVisibility(loading ? View.GONE : View.VISIBLE);
            progressSubmit.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!loading);
        });

        viewModel.getUploadResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.success) {
                String msg = "found".equals(postType)
                        ? getString(R.string.toast_item_posted_found)
                        : getString(R.string.toast_item_posted_lost);
                ToastUtils.show(requireContext(), msg, Toast.LENGTH_LONG);
                popBack();
            } else {
                tvFormError.setText(result.errorMessage);
                tvFormError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void popBack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}