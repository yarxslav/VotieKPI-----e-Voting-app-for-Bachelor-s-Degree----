import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {User} from '../../models/user.model';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Router, RouterLink} from '@angular/router';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {Voting} from '../../models/voting.model';
import {environment} from '../../../environments/environment';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-user-home',
  templateUrl: './user-home.component.html',
  standalone: true,
  imports: [RouterLink, NgForOf, NgIf, NgClass, FormsModule],
  styleUrls: ['./user-home.component.css'],
})

export class UserHomeComponent implements OnInit {
  currentTime: string;
  user: User | null;
  userRole: string;
  users: User[] = [];
  votings: Voting[] = [];
  filteredVotings: Voting[] = [];
  searchQuery: string = '';
  verificationStatus: string = '';
  isLoading: boolean = true;
  selectedStatus: string = 'ALL';
  selectedType: string = 'ALL';

  constructor(private authService: AuthService, private http: HttpClient, private router: Router) {
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
      if (user) {
        this.fetchVerificationStatus(user.id);
      }
    });
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString('uk-UA', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  }

  fetchVerificationStatus(userId: number): void {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue.token}`);
    this.http.get<any>(`${environment.baseUrl}/users/${userId}/status`, {headers})
      .subscribe(
        (response) => {
          this.verificationStatus = typeof response === 'string' ? response : String(response);
          console.log('Fetched verification status:', this.verificationStatus);
          if (this.verificationStatus === 'VERIFIED') {
            this.getVotings();
          } else {
            this.isLoading = false;
          }
        },
        (error) => {
          console.error('Error fetching verification status', error);
          this.isLoading = false;
        }
      );
  }

  getVotings(): void {
    this.http.get<Voting[]>(`${environment.baseUrl}/votings`).subscribe(
      (data) => {
        this.votings = data;
        console.log('Fetched votings:', this.votings);
        this.filterVotings();
        this.isLoading = false;
      },
      (error) => {
        console.error('Error fetching votings', error);
        this.isLoading = false;
      }
    );
  }

  logout(): void {
    this.authService.logout();
  }

  getStatusTranslation(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Активне';
      case 'COMPLETED':
        return 'Завершене';
      case 'SUSPENDED':
        return 'Призупинено';
      case 'SCHEDULED':
        return 'Планується';
      default:
        return status;
    }
  }

  getImageSrc(type: string): string {
    return type === 'Групове' ? 'assets/group.png' : 'assets/university.png';
  }

  getVotingTitle(voting: Voting): string {
    if (voting.category === 'Групове') {
      return `${voting.group} | ${voting.name}`;
    }
    return voting.name;
  }

  getStatusLabelClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'active-label';
      case 'COMPLETED':
        return 'finished-label';
      case 'SUSPENDED':
        return 'suspended-label';
      case 'SCHEDULED':
        return 'scheduled-label';
      default:
        return '';
    }
  }

  filterVotings(): void {
    if (!this.user) {
      return;
    }

    const userUniversity = this.user.university;
    const userGroup = this.user.group;

    this.filteredVotings = this.votings.filter(voting => {
      const universityMatch = voting.university === userUniversity;
      const groupMatch = voting.group === userGroup;
      const statusMatch = this.selectedStatus === 'ALL' || voting.status === this.selectedStatus;
      const typeMatch = this.selectedType === 'ALL' || voting.category === this.selectedType;

      if (typeMatch && voting.category === 'Загальноуніверситетське' && universityMatch && statusMatch) {
        return true;
      }
      return typeMatch && voting.category === 'Групове' && universityMatch && groupMatch && statusMatch;
    });
  }

  onStatusChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedStatus = selectElement.value;
    this.filterVotings();
  }

  onTypeChange(type: string): void {
    this.selectedType = type;
    this.filterVotings();
  }

  onSearch(): void {
    this.router.navigate(['/search-results'], {queryParams: {query: this.searchQuery}});
  }
}
