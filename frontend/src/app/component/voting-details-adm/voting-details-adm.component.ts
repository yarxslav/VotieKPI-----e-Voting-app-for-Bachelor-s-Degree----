import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {VotingService} from '../../services/voting.service';
import {CandidateService} from '../../services/candidate.service';
import {FormsModule} from '@angular/forms';
import {UNIVERSITIES} from '../../constants/universities';
import {NgForOf, NgIf} from '@angular/common';
import {ImageDto} from '../../models/image-dto.model';
import {User} from '../../models/user.model';
import {Voting} from '../../models/voting.model';

@Component({
  selector: 'app-voting-details-adm',
  templateUrl: './voting-details-adm.component.html',
  styleUrls: ['./voting-details-adm.component.css'],
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgForOf,
    RouterModule
  ],
})

export class VotingDetailsAdmComponent implements OnInit {
  currentTime: string;
  user: User | null;
  userRole: string;
  searchQuery: string = '';
  voting: Voting | null = null;
  suspendVoting: boolean = false;
  activateVoting: boolean = false;
  universities: string[] = UNIVERSITIES;
  candidateImages: { [key: number]: string } = {};
  modalImage: string | null = null;
  candidateFiles: { [key: number]: string } = {};
  showDeleteConfirmation: boolean = false;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private votingService: VotingService,
    private candidateService: CandidateService
  ) {
    this.currentTime = '';
    this.user = null;
    this.userRole = '';
  }

  ngOnInit(): void {
    const votingId = this.route.snapshot.paramMap.get('id');

    this.updateCurrentTime();
    setInterval(() => this.updateCurrentTime(), 60000);

    this.authService.currentUser.subscribe((user: User) => {
      this.user = user;
      this.userRole = user?.roles[0]?.name || '';
    });

    if (votingId) {
      this.votingService.getVotingById(votingId).subscribe((voting) => {
        this.voting = voting;
        this.initializeCheckBoxes();
        this.loadCandidateImages();
      });
    }
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

  initializeCheckBoxes(): void {
    if (this.voting?.status === 'ACTIVE') {
      this.suspendVoting = false;
    } else if (this.voting?.status === 'SUSPENDED') {
      this.activateVoting = false;
    }
  }

  onUniversityChange(selectedUniversity: string): void {
    if (this.voting) {
      this.voting.university = selectedUniversity;
    }
  }

  saveVoting(): void {
    if (this.voting) {
      if (this.suspendVoting) {
        this.voting.status = 'SUSPENDED';
      } else if (this.activateVoting) {
        this.voting.status = 'ACTIVE';
      }

      if (this.voting.category === 'Загальноуніверситетське') {
        this.voting.group = '';
      }

      const votingData = {
        voting: this.voting,
        candidateImages: Object.keys(this.candidateFiles).map(id => ({
          candidateId: parseInt(id, 10),
          image: this.candidateFiles[parseInt(id, 10)]
        }))
      };

      this.votingService.updateVoting(this.voting.id!.toString(), votingData).subscribe(
        (response) => {
          console.log('Voting updated successfully', response);
          alert('Голосування оновлено успішно');
        },
        (error) => {
          console.error('Error updating voting', error);
          alert('Помилка при оновленні голосування');
        }
      );
    }
  }

  addCandidate(): void {
    if (this.voting) {
      this.voting.candidates.push({
        id: null,
        name: '',
        surname: '',
        patronymic: '',
        imageName: '',
        speech: ''
      });
    }
  }

  removeCandidate(index: number): void {
    if (this.voting && this.voting.candidates.length > 1) {
      const confirmation = confirm('Ви впевнені, що хочете видалити цього кандидата?');
      if (confirmation) {
        this.voting.candidates.splice(index, 1);
      }
    } else {
      alert('Має бути щонайменше один кандидат');
    }
  }

  loadCandidateImages(): void {
    this.candidateService.getCandidateImages().subscribe(
      (images: ImageDto[]) => {
        this.candidateImages = {};
        images.forEach(image => {
          this.candidateImages[image.id] = 'data:image/jpeg;base64,' + image.image;
        });
      },
      (error) => {
        console.error('Error loading candidate images', error);
      }
    );
  }

  onFileSelected(event: any, candidate: any): void {
    const file = event.target.files[0];
    if (file && candidate.id != null) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.candidateFiles[candidate.id] = e.target.result.split(',')[1];
        this.candidateImages[candidate.id] = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  openModal(imageSrc: string): void {
    this.modalImage = imageSrc;
  }

  closeModal(): void {
    this.modalImage = null;
  }

  confirmDeleteVoting(): void {
    this.showDeleteConfirmation = true;
  }

  cancelDelete(): void {
    this.showDeleteConfirmation = false;
  }

  deleteVoting(): void {
    if (this.voting) {
      this.votingService.deleteVoting(this.voting.id!.toString()).subscribe(
        () => {
          console.log('Voting deleted successfully');
          alert('Голосування видалено успішно');
          this.router.navigate(['/admin-home-votings']);
        },
        (error) => {
          console.error('Error deleting voting', error);
          alert('Помилка при видаленні голосування');
        }
      );
    }
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], { queryParams: { query: this.searchQuery } });
  }

  logout(): void {
    this.authService.logout();
  }
}
