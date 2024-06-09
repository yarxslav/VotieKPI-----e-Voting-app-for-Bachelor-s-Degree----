import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {environment} from '../../../environments/environment';
import {AuthService} from '../../services/auth.service';
import {User} from '../../models/user.model';

@Component({
  selector: 'app-admin-user-details',
  templateUrl: './admin-user-details.component.html',
  standalone: true,
  styleUrls: ['./admin-user-details.component.css'],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ]
})
export class AdminUserDetailsComponent implements OnInit {
  currentTime: string;
  user: User;
  userId: string;
  searchQuery: string = '';
  userImage: string | null = null;
  showDeleteConfirmation: boolean = false;

  constructor(
    protected authService: AuthService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.currentTime = '';
    this.user = {
      id: 0,
      username: '',
      email: '',
      password: '',
      name: '',
      surname: '',
      patronymic: '',
      university: '',
      faculty: '',
      group: '',
      phone: '',
      inProgress: false,
      verificationData: {
        userStatus: 'NOT_VERIFIED',
        comment: ''
      },
      voterPublicId: '',
      roles: []
    };
    this.userId = '';
  }

  ngOnInit(): void {
    this.updateCurrentTime();
    setInterval(() => this.updateCurrentTime(), 60000);

    this.userId = this.route.snapshot.paramMap.get('id') || '';
    this.getUserDetails(this.userId);
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

  loadUserImage(userId: string): void {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue?.token}`);
    this.http.get(`${environment.baseUrl}/users/${userId}/image`, {
      headers,
      responseType: 'blob'
    }).subscribe(
      (response: Blob) => {
        if (response.size > 0) {
          const reader = new FileReader();
          reader.onload = () => {
            this.userImage = reader.result as string;
          };
          reader.readAsDataURL(response);
        } else {
          this.userImage = null;
        }
      },
      (error: any) => {
        console.error('Помилка завантаження зображення користувача', error);
        this.userImage = null;
      }
    );
  }

  getUserDetails(userId: string): void {
    this.http.get<User>(`${environment.baseUrl}/users/${userId}`).subscribe(
      (data) => {
        this.user = data;
        this.loadUserImage(userId);
      },
      (error) => {
        console.error('Error fetching user details', error);
      }
    );
  }

  updateVerificationStatus(newStatus: string): void {
    if (this.user && this.user.verificationData) {
      this.user.verificationData.userStatus = newStatus;
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  confirmDeleteUser(): void {
    this.showDeleteConfirmation = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
  }

  deleteUser(): void {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue?.token}`);
    this.http.delete(`${environment.baseUrl}/users/${this.userId}`, {headers}).subscribe(
      () => {
        console.log('User deleted successfully');
        this.router.navigate(['/admin-home-users']);
      },
      (error) => {
        console.error('Error deleting user', error);
      }
    );
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], { queryParams: { query: this.searchQuery } });
  }

  onSubmit(): void {
    if (this.user) {
      const updatedFields: any = {};
      if (this.user.name) updatedFields.name = this.user.name;
      if (this.user.surname) updatedFields.surname = this.user.surname;
      if (this.user.patronymic) updatedFields.patronymic = this.user.patronymic;
      if (this.user.university) updatedFields.university = this.user.university;
      if (this.user.faculty) updatedFields.faculty = this.user.faculty;
      if (this.user.group) updatedFields.group = this.user.group;
      if (this.user.verificationData?.userStatus) updatedFields.verificationData = {
        userStatus: this.user.verificationData.userStatus,
        comment: this.user.verificationData.comment
      };

      const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue?.token}`);
      this.http.patch(`${environment.baseUrl}/users/${this.userId}`, updatedFields, {headers}).subscribe(
        response => {
          alert('Зміни збережено успішно.');
        },
        error => {
          console.error('Error updating user', error);
          alert('Помилка при збереженні змін: ' + error.message);
        }
      );
    }
  }
}
