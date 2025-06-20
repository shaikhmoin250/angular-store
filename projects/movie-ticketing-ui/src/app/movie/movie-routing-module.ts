import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MovieListComponent } from './movie-list/movie-list.component'; // Generated name
import { MovieDetailsComponent } from './movie-details/movie-details.component'; // Manually added component

const routes: Routes = [
  { path: '', component: MovieListComponent },
  { path: ':id', component: MovieDetailsComponent } // Ensure this route is present and uncommented
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MovieRoutingModule { }
