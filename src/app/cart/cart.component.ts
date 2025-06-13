/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartItemDetails, StoreService } from '../store.service';
import { createPaymentDataRequest } from '../utils/payment.utils';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cart: CartItemDetails[] = [];

  paymentRequest!: google.payments.api.PaymentDataRequest;

  /**
   * Calculates the total number of items in the cart.
   * @returns {number} The total number of items.
   */
  get cartSize(): number {
    return this.cart.reduce((total, item) => total + item.quantity, 0);
  }

  /**
   * Calculates the total price of all items in the cart.
   * @returns {number} The total price.
   */
  get cartTotal(): number {
    return this.cart.reduce((total, item) => total + item.quantity * item.item.price, 0);
  }

  /**
   * Initializes the CartComponent.
   * @param {StoreService} storeService - Service for cart and store operations.
   * @param {Router} router - Angular router for navigation.
   */
  constructor(private storeService: StoreService, private router: Router) {
    this.paymentRequest = createPaymentDataRequest('0.00');
  }

  /**
   * Angular lifecycle hook called after component initialization.
   * Subscribes to cart updates and updates the payment request with the cart total.
   */
  ngOnInit(): void {
    this.storeService.getCart().subscribe(cart => {
      this.cart = cart;
      this.paymentRequest.transactionInfo.totalPrice = this.cartTotal.toFixed(2);
    });
  }

  /**
   * Navigates to the checkout page.
   */
  onCheckout(): void {
    this.router.navigate(['/checkout']);
  }

  /**
   * Removes an item from the cart.
   * @param {CartItemDetails} cartItem - The cart item to remove.
   */
  onRemove(cartItem: CartItemDetails): void {
    this.storeService.removeCartItem(cartItem);
  }

  /**
   * Handles the change in quantity for a cart item.
   * @param {Event} event - The input change event.
   * @param {CartItemDetails} cartItem - The cart item whose quantity is being changed.
   */
  onQuantityChange(event: Event, cartItem: CartItemDetails): void {
    const input = event.target as HTMLInputElement;
    this.storeService.updateCartItemQuantity({ ...cartItem, quantity: input.valueAsNumber });
  }

  /**
   * Handles the payment data loaded from the Google Pay button.
   * Processes the order, clears the cart, and navigates to the confirmation page.
   * @param {Event} event - The payment data load event, expected to be a CustomEvent.
   * @returns {Promise<void>}
   */
  async onLoadPaymentData(event: Event): Promise<void> {
    const paymentData = (event as CustomEvent<google.payments.api.PaymentData>).detail;
    await this.storeService.processOrder(this.cart, paymentData);

    this.storeService.setCart([]);
    this.router.navigate(['/confirm']);
  }
}
