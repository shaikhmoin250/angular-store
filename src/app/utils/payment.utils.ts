/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Utility function for creating Google Pay API PaymentDataRequest objects.
 * This file centralizes the creation of payment requests to ensure consistency
 * and ease of updates for payment parameters.
 */

// Using google.payments.api directly as it's globally available via script import in index.html
// If it were an importable module, we would import it.

/**
 * Creates a Google Pay PaymentDataRequest object configured for card payments.
 * This function initializes a payment request with standard parameters suitable for
 * processing card payments through a specified gateway.
 *
 * Note: Merchant ID, currency code, and country code are currently hardcoded
 * and marked with TODOs for future configuration via environment variables or dynamic inputs.
 *
 * @param {string} totalPrice - The total price for the transaction, formatted as a string (e.g., "10.00").
 * @returns {google.payments.api.PaymentDataRequest} The configured PaymentDataRequest object.
 * @see {@link https://developers.google.com/pay/api/web/reference/request-objects#PaymentDataRequest|Google Pay PaymentDataRequest}
 */
export function createPaymentDataRequest(totalPrice: string): google.payments.api.PaymentDataRequest {
  return {
    apiVersion: 2,
    apiVersionMinor: 0,
    allowedPaymentMethods: [
      {
        type: 'CARD',
        parameters: {
          allowedAuthMethods: ['PAN_ONLY', 'CRYPTOGRAM_3DS'],
          allowedCardNetworks: ['MASTERCARD', 'VISA'],
        },
        tokenizationSpecification: {
          type: 'PAYMENT_GATEWAY',
          parameters: {
            gateway: 'example',
            gatewayMerchantId: 'exampleGatewayMerchantId',
          },
        },
      },
    ],
    merchantInfo: {
      merchantId: '17613812255336763067', // TODO: Use environment variable
      merchantName: 'Demo Only (you will not be charged)',
    },
    transactionInfo: {
      totalPriceStatus: 'FINAL',
      totalPriceLabel: 'Total',
      totalPrice: totalPrice,
      currencyCode: 'USD', // TODO: Use environment variable or make dynamic
      countryCode: 'US', // TODO: Use environment variable or make dynamic
    },
  };
}
