import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {User} from '../../models/user.model';
import {RouterLink, Router} from "@angular/router";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-admin-cabinet',
  templateUrl: './admin-cabinet.component.html',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule
  ],
  styleUrls: ['./admin-cabinet.component.css']
})

export class AdminCabinetComponent implements OnInit {
  currentTime: string;
  user: User | null;
  userRole: string;
  searchQuery: string = '';

  constructor(private authService: AuthService, private router: Router) {
    this.currentTime = '';
    this.user = null;
    this.userRole = '';
  }

  ngOnInit(): void {
    this.updateCurrentTime();
    setInterval(() => this.updateCurrentTime(), 60000);

    this.authService.currentUser.subscribe((user: User) => {
      this.user = user;
      this.userRole = user?.roles[0]?.name || '';
    });
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString('uk-UA', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  logout(): void {
    this.authService.logout();
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], {queryParams: {query: this.searchQuery}});
  }
}
