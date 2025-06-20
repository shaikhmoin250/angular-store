import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
// Import components here when they are successfully generated
// e.g., import { BookingProcessComponent } from './booking-process/booking-process.component';

const routes: Routes = [
  // { path: 'checkout', component: BookingProcessComponent }, // Example
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BookingRoutingModule { }
