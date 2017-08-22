/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.util.TxData;

public class SendFragment extends Fragment {
    static final String TAG = "GenerateFragment";

    EditText etAddress;
    EditText etPaymentId;
    EditText etAmount;
    Button bSweep;
    Spinner sMixin;
    Spinner sPriority;
    Button bPrepareSend;
    Button bDispose;
    Button bPaymentId;
    LinearLayout llConfirmSend;
    TextView tvTxAmount;
    TextView tvTxFee;
    TextView tvTxDust;
    EditText etNotes;
    Button bSend;
    ProgressBar pbProgress;

    final static int Mixins[] = {4, 6, 8, 10, 13}; // must match the layout XML
    final static PendingTransaction.Priority Priorities[] =
            {PendingTransaction.Priority.Priority_Low,
                    PendingTransaction.Priority.Priority_Medium,
                    PendingTransaction.Priority.Priority_High}; // must match the layout XML

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.send_fragment, container, false);

        sMixin = (Spinner) view.findViewById(R.id.sMixin);
        sPriority = (Spinner) view.findViewById(R.id.sPriority);
        etAddress = (EditText) view.findViewById(R.id.etAddress);
        etPaymentId = (EditText) view.findViewById(R.id.etPaymentId);
        etAmount = (EditText) view.findViewById(R.id.etAmount);
        bSweep = (Button) view.findViewById(R.id.bSweep);
        bPrepareSend = (Button) view.findViewById(R.id.bPrepareSend);
        bPaymentId = (Button) view.findViewById(R.id.bPaymentId);
        bDispose = (Button) view.findViewById(R.id.bDispose);

        llConfirmSend = (LinearLayout) view.findViewById(R.id.llConfirmSend);
        tvTxAmount = (TextView) view.findViewById(R.id.tvTxAmount);
        tvTxFee = (TextView) view.findViewById(R.id.tvTxFee);
        tvTxDust = (TextView) view.findViewById(R.id.tvTxDust);
        etNotes = (EditText) view.findViewById(R.id.etNotes);
        bSend = (Button) view.findViewById(R.id.bSend);

        pbProgress = (ProgressBar) view.findViewById(R.id.pbProgress);

        etAddress.setRawInputType(InputType.TYPE_CLASS_TEXT);
        etPaymentId.setRawInputType(InputType.TYPE_CLASS_TEXT);
        etNotes.setRawInputType(InputType.TYPE_CLASS_TEXT);

//        etAddress.setText("9tDC52GsMjTNt4dpnRCwAF7ekVBkbkgkXGaMKTcSTpBhGpqkPX56jCNRydLq9oGjbbAQBsZhLfgmTKsntmxRd3TaJFYM2f8");
        boolean testnet = WalletManager.getInstance().isTestNet();
        if (!testnet) throw new IllegalStateException("Sending TX only on testnet. sorry.");

        Helper.showKeyboard(getActivity());
        etAddress.requestFocus();
        etAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    if (addressOk()) {
                        etPaymentId.requestFocus();
                    } // otherwise ignore
                    return true;
                }
                return false;
            }
        });
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (addressOk() && amountOk()) {
                    bPrepareSend.setEnabled(true);
                } else {
                    bPrepareSend.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        etPaymentId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    if (paymentIdOk()) {
                        etAmount.requestFocus();
                    } // otherwise ignore
                    return true;
                }
                return false;
            }
        });

        etAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (amountOk()) {
                        Helper.hideKeyboard(getActivity());
                        disableEdit();
                        prepareSend();
                    }
                    return true;
                }
                return false;
            }
        });
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (addressOk() && amountOk()) {
                    bPrepareSend.setEnabled(true);
                } else {
                    bPrepareSend.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        setPrepareButtonState();
        bPrepareSend.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(getActivity());
                disableEdit();
                prepareSend();
            }
        });

        bDispose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityCallback.onDisposeRequest();
                enableEdit();
            }
        });

        bPaymentId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etPaymentId.setText((activityCallback.generatePaymentId()));
            }
        });

        bSweep.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(getActivity());
                prepareSweep();
            }
        });

        etNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (amountOk()) {
                        Helper.hideKeyboard(getActivity());
                    }
                    return true;
                }
                return false;
            }
        });

        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSend.setEnabled(false);
                send();
            }
        });
        return view;
    }

    private void setPrepareButtonState() {
        if (addressOk() && amountOk() && (bSend.getVisibility() != View.VISIBLE)) {
            bPrepareSend.setEnabled(true);
        } else {
            bPrepareSend.setEnabled(false);
        }
    }

    private boolean addressOk() {
        String address = etAddress.getText().toString();
        if (WalletManager.getInstance().isTestNet()) {
            return ((address.length() == 95) && ("9A".indexOf(address.charAt(0)) >= 0));
        } else {
            return ((address.length() == 95) && ("4".indexOf(address.charAt(0)) >= 0));
        }
    }

    private boolean amountOk() {
        long amount = Wallet.getAmountFromString(etAmount.getText().toString());
        return (amount > 0);
    }

    private boolean paymentIdOk() {
        String paymentId = etPaymentId.getText().toString();
        return paymentId.isEmpty() || activityCallback.isPaymentIdValid(paymentId);
    }

    private void prepareSend() {
        String dst_addr = etAddress.getText().toString();
        String paymentId = etPaymentId.getText().toString();
        long amount = Wallet.getAmountFromString(etAmount.getText().toString());
        int mixin = Mixins[sMixin.getSelectedItemPosition()];
        int priorityIndex = sPriority.getSelectedItemPosition();
        PendingTransaction.Priority priority = Priorities[priorityIndex];
        //Log.d(TAG, dst_addr + "/" + paymentId + "/" + amount + "/" + mixin + "/" + priority.toString());
        TxData txData = new TxData(
                dst_addr,
                paymentId,
                amount,
                mixin,
                priority);
        showProgress();
        activityCallback.onPrepareSend(txData);
    }

    private void prepareSweep() {
        etAddress.setText(activityCallback.getWalletAddress());
        etPaymentId.setText("");
        etAmount.setText("");
        disableEdit();
        showProgress();
        activityCallback.onPrepareSweep();
    }

    private void disableEdit() {
        sMixin.setEnabled(false);
        sPriority.setEnabled(false);
        etAddress.setEnabled(false);
        etPaymentId.setEnabled(false);
        etAmount.setEnabled(false);
        bPaymentId.setEnabled(false);
        bSweep.setEnabled(false);
        bPrepareSend.setEnabled(false);
    }

    private void enableEdit() {
        sMixin.setEnabled(true);
        sPriority.setEnabled(true);
        etAddress.setEnabled(true);
        etPaymentId.setEnabled(true);
        etAmount.setEnabled(true);
        bPaymentId.setEnabled(true);
        bSweep.setEnabled(true);
        bPrepareSend.setEnabled(true);
        llConfirmSend.setVisibility(View.GONE);
    }

    private void send() {
        etNotes.setEnabled(false);
        String notes = etNotes.getText().toString();
        activityCallback.onSend(notes);
    }

    SendFragment.Listener activityCallback;

    public interface Listener {
        void onPrepareSend(TxData data);

        void onPrepareSweep();

        void onSend(String notes);

        String generatePaymentId();

        boolean isPaymentIdValid(String paymentId);

        String getWalletAddress();

        void onDisposeRequest();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SendFragment.Listener) {
            this.activityCallback = (SendFragment.Listener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    public void onCreatedTransaction(PendingTransaction pendingTransaction) {
        hideProgress();
        if (pendingTransaction == null) {
            enableEdit();
            return;
        }
        llConfirmSend.setVisibility(View.VISIBLE);
        tvTxAmount.setText(Wallet.getDisplayAmount(pendingTransaction.getAmount()));
        tvTxFee.setText(Wallet.getDisplayAmount(pendingTransaction.getFee()));
        tvTxDust.setText(Wallet.getDisplayAmount(pendingTransaction.getDust()));
    }

    public void onCreatedTransactionFailed(String errorText) {
        hideProgress();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.send_error_title));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enableEdit();
            }
        });
        builder.setMessage(errorText);
        builder.create().show();
    }

    public void showProgress() {
        pbProgress.setIndeterminate(true);
        pbProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        pbProgress.setVisibility(View.GONE);
    }
}
