import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, RouterModule } from "@angular/router";
import { NgForOf, CommonModule } from "@angular/common";
import { environment } from '../../../environments/environment';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-admin-home-users',
  templateUrl: './admin-home-users.component.html',
  standalone: true,
  imports: [
    NgForOf,
    CommonModule,
    RouterModule,
    FormsModule
  ],
  styleUrls: ['./admin-home-users.component.css']
})
export class AdminHomeUsersComponent implements OnInit {
  currentTime: string;
  user: User | null;
  userRole: string;
  searchQuery: string = '';
  users: User[] = [];

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) {
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

    this.getUsers();
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString('uk-UA', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  getUsers(): void {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.http.get<User[]>(`${environment.baseUrl}/users`, { headers }).subscribe(
      (data) => {
        this.users = data;
      },
      (error) => {
        console.error('Error fetching users', error);
      }
    );
  }

  getVerificationStatusText(status: string): string {
    switch (status) {
      case 'NOT_VERIFIED':
        return 'Не верифіковано';
      case 'VERIFIED':
        return 'Верифіковано';
      case 'IN_PROGRESS':
        return 'В обробці';
      case 'REJECTED':
        return 'Відхилено';
      default:
        return 'Невідомий статус';
    }
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], { queryParams: { query: this.searchQuery } });
  }

  viewUserDetails(user: User): void {
    this.router.navigate(['/admin-user-details', user.id], { state: { user } });
  }

  logout(): void {
    this.authService.logout();
  }
}
