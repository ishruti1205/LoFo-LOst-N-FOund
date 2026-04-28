package com.example.lofo.itemdetail;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.example.lofo.R;
import com.example.lofo.model.ItemPost;
import com.example.lofo.repository.ItemRepository;
import com.example.lofo.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class ItemDetailFragment extends BottomSheetDialogFragment {

    private final ItemRepository repository = new ItemRepository();

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static ItemDetailFragment newInstance(ItemPost post) {
        ItemDetailFragment sheet = new ItemDetailFragment();
        Bundle args = new Bundle();
        args.putString("postId",          post.getPostId());
        args.putString("title",           post.getTitle());
        args.putString("status",          post.getStatus());
        args.putString("category",        post.getCategory());
        args.putString("locationName",    post.getLocationName());
        args.putString("description",     post.getDescription());
        args.putString("imageUrl",        post.getImageUrl());
        args.putString("uploadedByUid",   post.getUploadedByUid());
        args.putString("uploadedByName",  post.getUploadedByName());
        args.putString("contactEmail",    post.getContactEmail());
        args.putString("contactNumber",   post.getContactNumber());
        args.putString("dateLostOrFound", post.getDateLostOrFound());
        if (post.getCreatedAt() != null)
            args.putLong("createdAtMs", post.getCreatedAt().toDate().getTime());
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public int getTheme() { return R.style.LoFo_BottomSheetDialog; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            View sheet = ((BottomSheetDialog) d)
                    .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet == null) return;
            sheet.setFitsSystemWindows(false);
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(sheet, null);
            if (sheet.getLayoutParams() instanceof android.widget.FrameLayout.LayoutParams) {
                android.widget.FrameLayout.LayoutParams lp =
                        (android.widget.FrameLayout.LayoutParams) sheet.getLayoutParams();
                lp.bottomMargin = 0;
                sheet.setLayoutParams(lp);
            }
            sheet.setPadding(sheet.getPaddingLeft(), sheet.getPaddingTop(),
                    sheet.getPaddingRight(), 0);
            View coord = (View) sheet.getParent();
            if (coord != null) {
                coord.setFitsSystemWindows(false);
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(coord, null);
            }
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setSkipCollapsed(false);
            behavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String postId     = args.getString("postId", "");
        String status     = args.getString("status", "lost");
        String posterUid  = args.getString("uploadedByUid", "");
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        boolean isOwner   = !posterUid.isEmpty() && posterUid.equals(currentUid);

        // Image
        ImageView ivImage = view.findViewById(R.id.ivDetailImage);
        String imageUrl = args.getString("imageUrl");
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this).load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_item).into(ivImage);
        }

        // Flag + text fields
        TextView tvFlag = view.findViewById(R.id.tvDetailFlag);
        applyFlag(tvFlag, status);

        setText(view, R.id.tvDetailTitle,    args.getString("title", ""));
        setText(view, R.id.tvDetailCategory, args.getString("category", ""));
        setText(view, R.id.tvDetailLocation, args.getString("locationName", ""));
        setText(view, R.id.tvDetailPostedBy,
                getString(R.string.posted_by_user, args.getString("uploadedByName", "Unknown")));

        String date = args.getString("dateLostOrFound");
        setText(view, R.id.tvDetailDate, !TextUtils.isEmpty(date) ? date : "—");
        long ms = args.getLong("createdAtMs", 0);
        if (ms > 0) setText(view, R.id.tvDetailTime, relativeTime(ms));

        // Email
        String email = args.getString("contactEmail");
        TextView tvEmail = view.findViewById(R.id.tvDetailEmail);
        tvEmail.setText(!TextUtils.isEmpty(email) ? email : "—");
        tvEmail.setAutoLinkMask(0);
        tvEmail.setLinksClickable(false);
        if (!TextUtils.isEmpty(email)) {
            tvEmail.setOnClickListener(v -> startActivity(Intent.createChooser(
                    new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email)), "Send Email")));
            tvEmail.setOnLongClickListener(v -> { copyToClipboard("Email", email); return true; });
        }

        // Phone
        String phone = args.getString("contactNumber");
        LinearLayout layoutPhone = view.findViewById(R.id.layoutPhone);
        TextView tvPhone = view.findViewById(R.id.tvDetailPhone);
        if (!TextUtils.isEmpty(phone)) {
            layoutPhone.setVisibility(View.VISIBLE);
            tvPhone.setText(phone);
            tvPhone.setAutoLinkMask(0);
            tvPhone.setLinksClickable(false);
            tvPhone.setOnClickListener(v -> {
                String cleanPhone = phone.replaceAll("[^0-9+]", ""); // remove spaces etc.
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + cleanPhone));
                startActivity(intent);
            });
            tvPhone.setOnLongClickListener(v -> { copyToClipboard("Phone", phone); return true; });
        } else {
            layoutPhone.setVisibility(View.GONE);
        }

        // Description
        String desc = args.getString("description");
        setText(view, R.id.tvDetailDescription,
                !TextUtils.isEmpty(desc) ? desc : getString(R.string.label_no_description));

        // ── Owner controls — custom bottom sheet options menu ──────────────────
        View ownerControls = view.findViewById(R.id.layoutOwnerControls);
        if (isOwner) {
            ownerControls.setVisibility(View.VISIBLE);
            view.findViewById(R.id.ivOptionsMenu).setOnClickListener(anchor ->
                    showOwnerOptionsMenu(postId, status, tvFlag, args));
        } else {
            ownerControls.setVisibility(View.GONE);
        }

        // Chat button
        MaterialButton btnChat = view.findViewById(R.id.btnChatWithPoster);
        if (isOwner) {
            btnChat.setVisibility(View.GONE);
        } else {
            btnChat.setOnClickListener(v ->
                    ToastUtils.show(requireContext(), getString(R.string.chat_coming_soon)));
        }
    }

    // ─── Custom owner options bottom sheet ────────────────────────────────────
    // Replaces PopupMenu entirely. Uses a BottomSheetDialog with our own layout
    // so we have full control over font, color, height, dividers.

    private void showOwnerOptionsMenu(String postId, String status,
                                      TextView tvFlag, Bundle args) {
        View menuView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_options_menu, null);

        BottomSheetDialog menuDialog = new BottomSheetDialog(
                requireContext(), R.style.LoFo_BottomSheetDialog);
        menuDialog.setContentView(menuView);

        // Apply same edge-to-edge fix
        menuDialog.setOnShowListener(d -> {
            View sheet = menuDialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (sheet == null) return;
            sheet.setFitsSystemWindows(false);
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(sheet, null);
            if (sheet.getLayoutParams() instanceof android.widget.FrameLayout.LayoutParams) {
                android.widget.FrameLayout.LayoutParams lp =
                        (android.widget.FrameLayout.LayoutParams) sheet.getLayoutParams();
                lp.bottomMargin = 0;
                sheet.setLayoutParams(lp);
            }
            sheet.setPadding(0, 0, 0, 0);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // Hide "Mark as Resolved" if already resolved
        View optionResolve = menuView.findViewById(R.id.optionMarkResolved);
        if ("resolved".equals(status)) {
            optionResolve.setVisibility(View.GONE);
            menuView.findViewById(R.id.dividerResolve).setVisibility(View.GONE);
        }

        optionResolve.setOnClickListener(v -> {
            menuDialog.dismiss();
            confirmMarkResolved(postId, status, tvFlag);
        });

        menuView.findViewById(R.id.optionEdit).setOnClickListener(v -> {
            menuDialog.dismiss();
            showEditDialog(args, postId);
        });

        menuView.findViewById(R.id.optionDelete).setOnClickListener(v -> {
            menuDialog.dismiss();
            confirmDelete(postId);
        });

        menuDialog.show();
    }

    // ─── Edit dialog ──────────────────────────────────────────────────────────
    // FIX: Cast to android.widget.EditText (not TextInputEditText).
    // When a layout is inflated inside an AlertDialog, Material's
    // TextInputEditText is created as AppCompatEditText by the AppCompat inflater
    // unless the dialog itself uses MaterialComponents theme. Casting to the
    // base EditText class works correctly in all cases.

    private void showEditDialog(Bundle args, String postId) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_item, null);

        EditText etEditItemName = dialogView.findViewById(R.id.etEditItemName);
        EditText etDesc     = dialogView.findViewById(R.id.etEditDescription);
        EditText etLocation = dialogView.findViewById(R.id.etEditLocation);
        EditText etDate     = dialogView.findViewById(R.id.etEditDate);
        EditText etPhone    = dialogView.findViewById(R.id.etEditPhone);

        // Pre-fill existing values
        etEditItemName.setText(args.getString("title", ""));
        etDesc.setText(args.getString("description", ""));
        etLocation.setText(args.getString("locationName", ""));
        etDate.setText(args.getString("dateLostOrFound", ""));
        etPhone.setText(args.getString("contactNumber", ""));

        TextInputLayout tilDate = dialogView.findViewById(R.id.tilEditDate);
        TextInputLayout tilLocation = dialogView.findViewById(R.id.tilEditLocation);

        String status = args.getString("status", "lost");

        if ("found".equals(status)) {
            tilLocation.setHint(getString(R.string.hint_location_found));
            tilDate.setHint(getString(R.string.hint_date_found));
        } else {
            tilLocation.setHint(getString(R.string.hint_location_lost));
            tilDate.setHint(getString(R.string.hint_date_lost));
        }

        TextView editFormTitle = dialogView.findViewById(R.id.tvDialogTitle);
        editFormTitle.setText("found".equals(status)
                ? getString(R.string.edit_found_item)
                : getString(R.string.edit_lost_item));

        // Date field opens DatePickerDialog
        // Open picker on icon click OR long click (optional UX)
        etDate.setOnLongClickListener(v -> {
            hideKeyboard(etDate);
            showAccentDatePicker(etDate);
            return true;
        });

        tilDate.setStartIconOnClickListener(v -> {
            hideKeyboard(etDate);
            showAccentDatePicker(etDate);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnEditCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnEditSave).setOnClickListener(v -> {
            String title    = getEtText(etEditItemName);
            String desc     = getEtText(etDesc);
            String location = getEtText(etLocation);
            String dateStr  = getEtText(etDate);
            String phone    = getEtText(etPhone);

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)
                    || TextUtils.isEmpty(location)) {
                ToastUtils.show(requireContext(), getString(R.string.error_edit_required));
                return;
            }

            if (!dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                ToastUtils.show(requireContext(), getString(R.string.error_edit_date));
                return;
            }

            repository.updateItemFields(postId, title, desc, location,
                            dateStr, phone.isEmpty() ? null : phone)
                    .observe(getViewLifecycleOwner(), result -> {
                        if (result != null && result.success) {
                            ToastUtils.show(requireContext(), getString(R.string.toast_item_updated));
                            dialog.dismiss();
                            dismiss();
                        } else {
                            ToastUtils.show(requireContext(), getString(R.string.error_update_failed));
                        }
                    });
        });

        dialog.show();
    }

    private void showAccentDatePicker(EditText etDate) {
        hideKeyboard(etDate); // ensure keyboard closes

        Calendar cal = Calendar.getInstance();

        String existing = etDate.getText() != null
                ? etDate.getText().toString().trim() : "";

        if (existing.length() == 10) {
            try {
                String[] p = existing.split("/");
                cal.set(
                        Integer.parseInt(p[2]),
                        Integer.parseInt(p[1]) - 1,
                        Integer.parseInt(p[0])
                );
            } catch (Exception ignored) {}
        }

        DatePickerDialog picker = new DatePickerDialog(
                requireContext(),
                R.style.LoFo_DatePickerDialog,
                (v, year, month, day) -> etDate.setText(
                        String.format(java.util.Locale.getDefault(),
                                "%02d/%02d/%04d", day, month + 1, year)
                ),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.show();
    }

    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ─── Mark as Resolved ─────────────────────────────────────────────────────

    private void confirmMarkResolved(String postId, String currentStatus, TextView tvFlag) {

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_resolve_title))
                .setMessage(getString(R.string.dialog_resolve_message))
                .setPositiveButton(getString(R.string.btn_mark_resolved), (d, w) ->
                        repository.updateStatus(postId, "resolved")
                                .observe(getViewLifecycleOwner(), result -> {
                                    if (result != null && result.success) {
                                        applyFlag(tvFlag, "resolved");

                                        // Notify parent to refresh list
                                        getParentFragmentManager().setFragmentResult(
                                                "refresh_posts",
                                                new Bundle()
                                        );

                                        ToastUtils.show(requireContext(),
                                                getString(R.string.toast_status_resolved));

                                        if ("lost".equals(currentStatus)) {
                                            showBuyMeCoffeePrompt();
                                        } else {
                                            dismiss();
                                        }
                                    } else {
                                        ToastUtils.show(requireContext(),
                                                getString(R.string.error_update_failed));
                                    }
                                }))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .create();

        dialog.show();

        // Set button colors AFTER show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.color_accent));

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(requireContext().getColor(R.color.icon_tint));
    }

    private void showBuyMeCoffeePrompt() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("🎉 " + getString(R.string.dialog_coffee_title))
                .setMessage(getString(R.string.dialog_coffee_message))
                .setPositiveButton(getString(R.string.btn_coffee_yes), (d, w) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://buymeacoffee.com/YOUR_USERNAME")));
                    dismiss();
                })
                .setNegativeButton(getString(R.string.btn_coffee_no), (d, w) -> dismiss())
                .setCancelable(false)
                .create();

        dialog.show();

        // Set colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.color_accent));

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(requireContext().getColor(R.color.icon_tint));
    }

    private void confirmDelete(String postId) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_message))
                .setPositiveButton(getString(R.string.btn_delete), (d, w) ->
                        repository.deleteItem(postId)
                                .observe(getViewLifecycleOwner(), result -> {
                                    if (result != null && result.success) {
                                        ToastUtils.show(requireContext(),
                                                getString(R.string.toast_item_deleted));
                                        dismiss();
                                    } else {
                                        ToastUtils.show(requireContext(),
                                                getString(R.string.error_delete_failed));
                                    }
                                }))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .create();

        dialog.show();

        // Set colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.color_flag_lost)); // red for delete

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(requireContext().getColor(R.color.icon_tint));

        // HANDLE CLICK MANUALLY
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            repository.deleteItem(postId)
                    .observe(getViewLifecycleOwner(), result -> {
                        if (result != null && result.success) {

                            ToastUtils.show(requireContext(),
                                    getString(R.string.toast_item_deleted));

                            // Notify parent to refresh
                            getParentFragmentManager().setFragmentResult(
                                    "refresh_posts",
                                    new Bundle()
                            );

                            dialog.dismiss(); // CLOSE DIALOG
                            dismiss();        // close bottom sheet

                        } else {
                            ToastUtils.show(requireContext(),
                                    getString(R.string.error_delete_failed));

                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    });
        });
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager cm = (ClipboardManager)
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText(label, text));
        ToastUtils.show(requireContext(), label + " copied to clipboard");
    }

    private void applyFlag(TextView tv, String status) {
        switch (status) {
            case "found":    tv.setText("FOUND");    tv.setBackgroundResource(R.drawable.bg_flag_found);    break;
            case "resolved": tv.setText("RESOLVED"); tv.setBackgroundResource(R.drawable.bg_flag_resolved); break;
            default:         tv.setText("LOST");     tv.setBackgroundResource(R.drawable.bg_flag_lost);
        }
    }

    private void setText(View root, int id, String text) {
        ((TextView) root.findViewById(id)).setText(text);
    }

    /** Use EditText (base class) not TextInputEditText to avoid ClassCastException */
    private String getEtText(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String relativeTime(long millis) {
        long diff = System.currentTimeMillis() - millis;
        long mins = diff / 60_000, hours = diff / 3_600_000,
                days = diff / 86_400_000, months = days / 30, years = days / 365;
        if (mins  < 1)   return "Just now";
        if (mins  < 60)  return mins + " min ago";
        if (hours < 24)  return hours + " hr" + (hours == 1 ? "" : "s") + " ago";
        if (days  == 1)  return "Yesterday";
        if (days  < 30)  return days + " days ago";
        if (months < 12) return months + " month" + (months == 1 ? "" : "s") + " ago";
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }
}