import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router'; // Will be needed

@Component({
  selector: 'app-movie-details',
  templateUrl: './movie-details.component.html',
  styleUrls: ['./movie-details.component.scss']
})
export class MovieDetailsComponent implements OnInit {
  movieId: string | null = null;

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.movieId = this.route.snapshot.paramMap.get('id');
    // TODO: Fetch movie details based on movieId
    console.log('Movie ID:', this.movieId);
  }
}
