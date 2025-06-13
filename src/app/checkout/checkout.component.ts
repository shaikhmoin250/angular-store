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
import { StoreService } from '../store.service';
import { NgForm } from '@angular/forms';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
  firstName: string = '';
  lastName: string = '';
  address1: string = '';
  address2: string = '';
  city: string = '';
  state: string = '';
  zip: string = '';
  country: string = '';
  cardName: string = '';
  cardNumber: string = '';
  expDate: string = '';
  cvv: string = '';

  /**
   * Initializes the CheckoutComponent.
   * @param {Router} router - Angular router for navigation.
   * @param {StoreService} storeService - Service for cart and order processing.
   */
  constructor(private router: Router, private storeService: StoreService) {}

  /**
   * Angular lifecycle hook that is called after data-bound properties of a directive are initialized.
   * Currently, this method is empty as no specific actions are needed on component initialization
   * beyond what Angular provides.
   */
  ngOnInit(): void {}

  /**
   * Handles the submission of the checkout form.
   * Prevents default form submission, processes the order with items from the cart
   * and mocked payment/shipping details, then navigates to the confirmation page.
   * @param {Event} event - The form submission event.
   * @param {NgForm} _form - The Angular form object (marked as unused with an underscore).
   * @returns {Promise<void>}
   */
  async onSubmit(event: Event, _form: NgForm): Promise<void> {
    event.preventDefault();

    this.storeService
      .getCart()
      .pipe(first())
      .subscribe(
        async cartItems => {
          await this.storeService.processOrder(cartItems, {
            apiVersion: 2, // Added apiVersion
            apiVersionMinor: 0, // Added apiVersionMinor
            shippingAddress: {
              address1: this.address1,
              address2: this.address2,
              administrativeArea: this.state,
              countryCode: this.country,
              locality: this.city,
              postalCode: this.zip
            },
            paymentMethodData: {
              type: 'CARD',
              description: `Card: ${this.cardNumber.slice(-4)}`,
              tokenizationData: { // Added mock tokenizationData
                type: 'PAYMENT_GATEWAY',
                token: 'exampleToken'
              }
            }
          });
        },
        _error => {},
        () => {
          this.storeService.setCart([]);
          this.router.navigate(['/confirm']);
        }
      );
  }
}
