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
import { ActivatedRoute, Router } from '@angular/router';
import { ItemDetails, StoreService } from '../store.service';
import { createPaymentDataRequest } from '../utils/payment.utils';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Unescapes HTML entities in a text string.
 * This uses the browser's DOM parsing capabilities to decode HTML entities.
 * @param {string} text - The text containing HTML entities to unescape.
 * @returns {string} The unescaped text.
 */
function unescapeHtml(text: string): string {
  const elem = document.createElement('textarea');
  elem.innerHTML = text;
  return elem.textContent || '';
}

@Component({
  selector: 'app-item-details',
  templateUrl: './item-details.component.html',
  styleUrls: ['./item-details.component.scss']
})
export class ItemDetailsComponent implements OnInit {
  item!: ItemDetails;
  size = 'M';
  sizeOptions = ['XS', 'S', 'M', 'L', 'XL'];
  quantity = 1;
  quantityOptions = [1, 2, 3, 4, 5];

  paymentRequest!: google.payments.api.PaymentDataRequest;

  /**
   * Gets the unescaped HTML description of the item.
   * @returns {string} The unescaped item description.
   */
  get itemDescription(): string {
    return unescapeHtml(this.item.description);
  }

  /**
   * Initializes the ItemDetailsComponent.
   * @param {StoreService} storeService - Service for accessing store data.
   * @param {ActivatedRoute} route - Service for accessing route parameters.
   * @param {MatSnackBar} snackBar - Service for displaying snack bar notifications.
   * @param {Router} router - Angular router for navigation.
   */
  constructor(
    private storeService: StoreService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  /**
   * Angular lifecycle hook called after component initialization.
   * Fetches item details based on route parameters and sets up the payment request.
   */
  ngOnInit(): void {
    this.storeService
      .getItem(this.route.snapshot.paramMap.get('listId')!, this.route.snapshot.paramMap.get('itemId')!)
      .subscribe(item => {
        this.item = item!;

        this.paymentRequest = createPaymentDataRequest(this.item.price.toFixed(2));
      });
  }

  /**
   * Adds the current item to the shopping cart and displays a notification.
   * Provides an action to navigate to the cart.
   */
  onAddToCart(): void {
    this.storeService.addItemToCart(this.item, this.size, this.quantity);
    const snackbar = this.snackBar.open(`${this.item.title} added to cart.`, 'view cart', {
      duration: 5000
    });
    snackbar.onAction().subscribe(() => {
      this.router.navigate(['/cart']);
    });
  }

  /**
   * Handles the payment data loaded from the Google Pay button for a single item purchase.
   * Processes the order with the current item, then navigates to the confirmation page.
   * @param {Event} event - The payment data load event, expected to be a CustomEvent.
   * @returns {Promise<void>}
   */
  async onLoadPaymentData(event: Event): Promise<void> {
    const paymentData = (event as CustomEvent<google.payments.api.PaymentData>).detail;
    await this.storeService.processOrder(
      [
        {
          item: this.item,
          quantity: this.quantity,
          size: this.size
        }
      ],
      paymentData
    );

    this.router.navigate(['/confirm']);
  }
}
