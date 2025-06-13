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

@Component({
  selector: 'app-confirmation',
  templateUrl: './confirmation.component.html',
  styleUrls: ['./confirmation.component.scss']
})
export class ConfirmationComponent implements OnInit {
  /**
   * Initializes the ConfirmationComponent.
   * @param {Router} router - Angular router for navigation.
   */
  constructor(private router: Router) {}

  /**
   * Angular lifecycle hook that is called after data-bound properties of a directive are initialized.
   * Currently, this method is empty as no specific actions are needed on component initialization.
   */
  ngOnInit(): void {}

  /**
   * Handles the click event for the "Continue Shopping" button.
   * Navigates the user back to the home page.
   */
  onContinue(): void {
    this.router.navigate(['/']);
  }
}
