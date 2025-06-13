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
import { CategoryDetails } from '../store.service';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit {
  /**
   * Input property for the category details to be displayed by this component.
   * Expected to be provided by the parent component.
   */
  @Input() category!: CategoryDetails;

  /**
   * Initializes the CategoryComponent.
   * Currently, this constructor is empty as no specific initialization logic is needed
   * beyond what Angular provides.
   */
  constructor() {}

  /**
   * Angular lifecycle hook that is called after data-bound properties of a directive are initialized.
   * Currently, this method is empty as no specific actions are needed on component initialization.
   */
  ngOnInit(): void {}
}
