import { Routes } from '@angular/router';

export const routes: Routes = [
  // Redirect to movies for home for now
  { path: '', redirectTo: '/movies', pathMatch: 'full' },
  // Lazy load feature modules
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth-module').then(m => m.AuthModule)
  },
  {
    path: 'movies',
    loadChildren: () => import('./movie/movie-module').then(m => m.MovieModule)
  },
  // User and Booking modules are commented out as their components were not fully generated
  // {
  //   path: 'user',
  //   loadChildren: () => import('./user/user-module').then(m => m.UserModule)
  // },
  // {
  //   path: 'booking',
  //   loadChildren: () => import('./booking/booking-module').then(m => m.BookingModule)
  // },
  // Potentially a HomeComponent if needed later: { path: 'home', component: HomeComponent },
];
