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

import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartItemDetails, CategoryDetails, StoreService } from '../store.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  /**
   * Input property for the list of categories to display in the header menu.
   * Expected to be provided by the parent component.
   */
  @Input() categories!: CategoryDetails[];
  cart: CartItemDetails[] = [];

  /**
   * Calculates the total number of items currently in the shopping cart.
   * @returns {number} The total number of items in the cart.
   */
  get cartSize(): number {
    return this.cart.reduce((total, item) => total + item.quantity, 0);
  }

  /**
   * Initializes the HeaderComponent.
   * @param {Router} router - Angular router for navigation.
   * @param {StoreService} storeService - Service for accessing cart and store data.
   */
  constructor(private router: Router, private storeService: StoreService) {}

  /**
   * Handles click events on category menu items.
   * Navigates to the item list page for the selected category.
   * @param {Event} event - The click event (marked as unused, but present for event binding).
   * @param {CategoryDetails} category - The category that was clicked.
   */
  onMenuClick(_event: Event, category: CategoryDetails): void {
    this.router.navigate(['/list', category.name]);
  }

  /**
   * Handles the click event for the shopping cart icon.
   * Navigates to the cart page.
   */
  onCartClick(): void {
    this.router.navigate(['/cart']);
  }

  /**
   * Angular lifecycle hook that is called after data-bound properties of a directive are initialized.
   * Subscribes to cart updates from the StoreService to keep the cart display current.
   */
  ngOnInit(): void {
    this.storeService.getCart().subscribe(cart => {
      this.cart = cart;
    });
  }
}
