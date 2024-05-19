var exec = cordova.require('cordova/exec');


const NombaTerminalActions = {
    /*
              Specify the amount to be charged.
              If you intend to pass, for example, ₦99.9,
              you need to input the amount string as 999.
    */
    triggerCardPayment: function (paymentData, receiptData, success, error) {
        console.log("triggerCardPayment clicked");
        exec(success, error, "NombaTerminalPlugin", "terminalRequest", [
            "triggerCardPayment",
            paymentData.amount,
            paymentData.reference,
            JSON.stringify(receiptData),
        ]);
    },

    /*
              Specify the amount to be charged.
              If you intend to pass, for example, ₦99.9,
              you need to input the amount string as 99.9.
    */
    triggerPayByTransfer: function (arg0, success, error) {
        exec(success, error, "NombaTerminalPlugin", "terminalRequest", [
            "triggerPayByTransfer",
            arg0.amount,
            arg0.reference,
            JSON.stringify(receiptData),
        ]);
    },
    /*
              Specify the amount to be charged.
              If you intend to pass, for example, ₦99.9,
              you need to input the amount string as 999.
    */
    triggerCardPlusPBT: function (arg0, success, error) {
        exec(success, error, "NombaTerminalPlugin", "terminalRequest", [
            "triggerCardAndPBT",
            arg0.amount,
            arg0.reference,
            JSON.stringify(receiptData),
        ]);
    },
    triggerPrintReceipt: function (success, error) {
        var customPrintData = [
            {
                content: "REPRINT",
                contentType: "TEXT",
                alignment: "CENTER",
                fontSize: "NORMAL",
            },
            {
                content: "MERCHANT COPY",
                contentType: "TEXT",
                alignment: "CENTER",
                fontSize: "NORMAL",
            },
            {
                content: "MERCHANT NAME: The Horseman",
                contentType: "TEXT",
                alignment: "LEFT",
                fontSize: "NORMAL",
                isBold: true,
            },
        ];
        exec(success, error, "NombaTerminalPlugin", "terminalRequest", ["triggerPrintCustomReceipt", JSON.stringify(customPrintData)]);
    }
};

module.exports = NombaTerminalActions