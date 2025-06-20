import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MovieRoutingModule } from './movie-routing-module'; // Generated name
import { MovieListComponent } from './movie-list/movie-list.component'; // Generated component
import { MovieCardComponent } from './movie-card/movie-card.component'; // Generated component
import { MovieDetailsComponent } from './movie-details/movie-details.component'; // Manually added component

@NgModule({
  declarations: [
    MovieListComponent,
    MovieCardComponent,
    MovieDetailsComponent // Add new component here
  ],
  imports: [
    CommonModule,
    MovieRoutingModule
  ]
  // exports: [ MovieDetailsComponent ] // Only if used directly by other modules outside of routing
})
export class MovieModule { }
