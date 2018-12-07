import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortableTableDirective } from './layout/sortable-table.directive';
import { SortableColumnComponent } from "./layout/sortable-column.component";
import { TranslateModule } from '@ngx-translate/core';
import { PerunFooterComponent } from './layout/perun-footer/perun-footer.component';
import { FontAwesomeModule } from "@fortawesome/angular-fontawesome";
import { PerunHeaderComponent } from "./layout/perun-header/perun-header.component";
import { PerunSidebarComponent } from "./layout/perun-sidebar/perun-sidebar.component";
import { RouterModule } from "@angular/router";
import {FormsModule} from "@angular/forms";
import {
  MatButtonModule,
  MatCheckboxModule,
  MatIconModule,
  MatInputModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule,
  MatTableModule,
  MatToolbarModule,
  MatTooltipModule
} from "@angular/material";

@NgModule({
  imports: [
    RouterModule,
    CommonModule,
    FontAwesomeModule,
    FormsModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatTooltipModule,
    MatButtonModule,
    MatToolbarModule,
    MatSnackBarModule,
    MatIconModule,
    MatTableModule,
    MatSortModule,
    TranslateModule,
    MatSidenavModule
  ],
  declarations: [
    SortableColumnComponent,
    SortableTableDirective,
    PerunHeaderComponent,
    PerunSidebarComponent,
    PerunFooterComponent
  ],
  exports: [
    FontAwesomeModule,
    SortableColumnComponent,
    SortableTableDirective,
    TranslateModule,
    PerunHeaderComponent,
    PerunSidebarComponent,
    PerunFooterComponent,
    FormsModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatTooltipModule,
    MatButtonModule,
    MatToolbarModule,
    MatSnackBarModule,
    MatIconModule,
    MatTableModule,
    MatSortModule,
    MatSidenavModule
  ]
})
export class SharedModule { }
