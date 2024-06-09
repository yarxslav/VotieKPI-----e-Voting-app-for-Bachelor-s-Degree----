import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCabinetComponent } from './admin-cabinet.component';

describe('AdminCabinetComponent', () => {
  let component: AdminCabinetComponent;
  let fixture: ComponentFixture<AdminCabinetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCabinetComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminCabinetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
