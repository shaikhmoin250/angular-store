import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
// Import components here when they are successfully generated
// e.g., import { UserProfileComponent } from './user-profile/user-profile.component';

const routes: Routes = [
  // { path: 'profile', component: UserProfileComponent }, // Example
  // { path: 'bookings', component: UserBookingsListComponent }, // Example
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
