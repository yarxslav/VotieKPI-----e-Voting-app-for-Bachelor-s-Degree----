import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHomeVotingsComponent } from './admin-home-votings.component';

describe('AdminHomeVotingsComponent', () => {
  let component: AdminHomeVotingsComponent;
  let fixture: ComponentFixture<AdminHomeVotingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHomeVotingsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminHomeVotingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
