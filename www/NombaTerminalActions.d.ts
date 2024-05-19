export type PaymentInterface = {
    amount: string;
    reference: string;
  }

export type ReceiptData = {
  email: boolean;
  sms: boolean;
  print: boolean;
}

export interface NombaTerminalActionsPlugin {
    triggerCardPayment(paymentData: PaymentInterface, receiptData: ReceiptData, success: () => void,
    fail: (error) => void): void;

    triggerPayByTransfer(paymentData: PaymentInterface, receiptData: ReceiptData, success: () => void,
    fail: (error) => void): void;

    triggerCardPlusPBT(paymentData: PaymentInterface, receiptData: ReceiptData, success: () => void,
    fail: (error) => void): void;

    triggerPrintReceipt(success: () => void,
    fail: (error) => void): void;
}

declare const NombaTerminalActions: NombaTerminalActionsPlugin

export default NombaTerminalActions;