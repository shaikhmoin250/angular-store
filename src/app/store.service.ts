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

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, from, Observable, Subject } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

export interface CategoryDetails {
  name: string;
  title: string;
  image: string;
}

export interface ItemDetails {
  name: string;
  title: string;
  category: string;
  price: number;
  description: string;
  image: string;
  largeImage: string;
}

export interface CartItemDetails {
  item: ItemDetails;
  size: string;
  quantity: number;
}

const categories: CategoryDetails[] = [
  {
    name: 'mens_outerwear',
    title: 'Mens Outerwear',
    image: '/assets/images/categories/mens_outerwear.jpg'
  },
  {
    name: 'ladies_outerwear',
    title: 'Ladies Outerwear',
    image: '/assets/images/categories/ladies_outerwear.jpg'
  },
  {
    name: 'mens_tshirts',
    title: 'Mens T-Shirts',
    image: '/assets/images/categories/mens_tshirts.jpg'
  },
  {
    name: 'ladies_tshirts',
    title: 'Ladies T-Shirts',
    image: '/assets/images/categories/ladies_tshirts.jpg'
  }
];

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private itemsCache: Map<string, Observable<ItemDetails[]>>;
  private cart: CartItemDetails[];
  private cartSubject?: Subject<CartItemDetails[]>;

  /**
   * Initializes the StoreService.
   * @param {HttpClient} http - The Angular HttpClient for making HTTP requests.
   */
  constructor(private http: HttpClient) {
    this.itemsCache = new Map();
    this.cart = [];
  }

  /**
   * Retrieves an item from localStorage.
   * @private
   * @template T The type of the item to retrieve.
   * @param {string} key - The key of the item in localStorage.
   * @returns {T} The retrieved item, or null if not found.
   */
  private getStorage<T>(key: string): T {
    return JSON.parse(localStorage.getItem(key) || 'null');
  }

  /**
   * Stores an item in localStorage.
   * @private
   * @template T The type of the item to store.
   * @param {string} key - The key under which to store the item.
   * @param {T} value - The item to store.
   */
  private setStorage<T>(key: string, value: T): void {
    localStorage.setItem(key, JSON.stringify(value));
  }

  /**
   * Retrieves all available shopping categories.
   * Includes a console log for debugging purposes.
   * @returns {Observable<CategoryDetails[]>} An observable emitting an array of category details.
   */
  getCategories(): Observable<CategoryDetails[]> {
    console.log(categories);
    return from([categories]);
  }

  /**
   * Retrieves items belonging to a specific category.
   * Implements a simple caching mechanism for item lists.
   * @param {string} name - The name of the category (e.g., 'mens_outerwear').
   * @returns {Observable<ItemDetails[]>} An observable emitting an array of item details for the category.
   */
  getItemsByCategory(name: string): Observable<ItemDetails[]> {
    let cat = this.itemsCache.get(name);
    if (cat) return cat;

    cat = this.http.get<ItemDetails[]>(`/assets/data/${name}.json`);
    this.itemsCache.set(name, cat);

    return cat;
  }

  /**
   * Retrieves a specific item by its category and name.
   * @param {string} category - The category name of the item.
   * @param {string} name - The name of the item.
   * @returns {Observable<ItemDetails | undefined>} An observable emitting the item details or undefined if not found.
   */
  getItem(category: string, name: string): Observable<ItemDetails | undefined> {
    return this.getItemsByCategory(category).pipe(mergeMap(items => from([items.find(item => item.name === name)])));
  }

  /**
   * Adds an item to the shopping cart or updates its quantity if already present.
   * @param {ItemDetails} item - The item to add.
   * @param {string} size - The selected size for the item.
   * @param {number} quantity - The quantity of the item to add.
   */
  addItemToCart(item: ItemDetails, size: string, quantity: number): void {
    let existing = this.cart.find(c => c.item.name === item.name && c.size === size);

    if (!existing) {
      this.cart = [
        ...this.cart,
        {
          item,
          size,
          quantity
        }
      ];
    } else {
      existing.quantity += quantity;
      this.cart = [...this.cart];
    }

    this.setCart(this.cart);
  }

  /**
   * Removes a specific item from the shopping cart.
   * @param {CartItemDetails} cartItem - The cart item to remove.
   */
  removeCartItem(cartItem: CartItemDetails): void {
    this.cart = this.cart.filter(c => !(c.item.name === cartItem.item.name && c.size === cartItem.size));

    this.setCart(this.cart);
  }

  /**
   * Updates the quantity of a specific item in the shopping cart.
   * If the item is not found, it's added to the cart.
   * @param {CartItemDetails} cartItem - The cart item with the updated quantity.
   */
  updateCartItemQuantity(cartItem: CartItemDetails): void {
    let existing = this.cart.find(c => c.item.name === cartItem.item.name && c.size === cartItem.size);

    if (!existing) {
      this.cart = [...this.cart, cartItem];
    } else {
      existing.quantity = cartItem.quantity;
      this.cart = [...this.cart];
    }

    this.setCart(this.cart);
  }

  /**
   * Retrieves the current state of the shopping cart as an observable.
   * Initializes the cart from localStorage if not already done.
   * @returns {Observable<CartItemDetails[]>} An observable emitting the array of cart items.
   */
  getCart(): Observable<CartItemDetails[]> {
    if (!this.cartSubject) {
      this.cart = this.getStorage<CartItemDetails[]>('cart') || [];
      this.cartSubject = new BehaviorSubject(this.cart);
    }
    return this.cartSubject;
  }

  /**
   * Updates the shopping cart with a new set of items and persists it to localStorage.
   * Notifies subscribers about the cart update.
   * @param {CartItemDetails[]} cart - The new array of cart items.
   */
  setCart(cart: CartItemDetails[]): void {
    this.cart = cart;
    this.cartSubject!.next(cart); // cartSubject is guaranteed to be initialized by getCart() before setCart() is typically called in flows.
    this.setStorage('cart', cart);
  }

  /**
   * Mock service to process an order.
   * This function simulates sending order details to a server.
   * Currently, it only logs the order details to the console.
   *
   * @param {CartItemDetails[]} cart - An array of items in the cart.
   * @param {google.payments.api.PaymentData} paymentData - The payment data object, typically from Google Pay.
   * @returns {Promise<{orderId: string}>} A promise that resolves with a mock order ID.
   */
  processOrder(cart: CartItemDetails[], paymentData: google.payments.api.PaymentData): Promise<{orderId: string}> {
    console.log(
      'TODO: send order to server',
      cart,
      paymentData.shippingAddress,
      paymentData.shippingOptionData?.id,
      paymentData.paymentMethodData
    );

    return Promise.resolve({
      orderId: Date.now().valueOf().toString()
    });
  }
}
