package com.example.course_service.service;

import com.example.course_service.client.StudentClient;
import com.example.course_service.entity.Course;
import com.example.course_service.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentClient studentClient;

    public CourseService(CourseRepository courseRepository, StudentClient studentClient) {
        this.courseRepository = courseRepository;
        this.studentClient = studentClient;
    }

    // Create a new course
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    // Retrieve all courses
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Retrieve a course by ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    // Update a course
    public Course updateCourse(Long id, Course updatedCourse) {
        Course existingCourse = getCourseById(id);
        existingCourse.setName(updatedCourse.getName());
        existingCourse.setTeacher(updatedCourse.getTeacher());
        existingCourse.setCredit(updatedCourse.getCredit());
        existingCourse.setEnrolledStudentIds(updatedCourse.getEnrolledStudentIds());
        return courseRepository.save(existingCourse);
    }

    // Delete a course
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // Retrieve course details along with enrolled students
    public CourseDetailsDTO getCourseWithStudents(Long id) {
        Course course = getCourseById(id);
        List<StudentClient.StudentDTO> enrolledStudents = new ArrayList<>();

        if (course.getEnrolledStudentIds() != null && !course.getEnrolledStudentIds().isEmpty()) {
            String[] studentIds = course.getEnrolledStudentIds().split(",");
            for (String sid : studentIds) {
                try {
                    Long studentId = Long.parseLong(sid.trim());
                    StudentClient.StudentDTO student = studentClient.getStudentById(studentId);
                    enrolledStudents.add(student);
                } catch (NumberFormatException e) {
                    // Log and ignore invalid IDs
                    System.err.println("Invalid student ID: " + sid);
                } catch (Exception e) {
                    // Log and handle exceptions from Student Service
                    System.err.println("Error fetching student with ID " + sid + ": " + e.getMessage());
                }
            }
        }

        CourseDetailsDTO dto = new CourseDetailsDTO();
        dto.setCourse(course);
        dto.setEnrolledStudents(enrolledStudents);
        return dto;
    }

    // DTO for course details with enrolled students
    public static class CourseDetailsDTO {
        private Course course;
        private List<StudentClient.StudentDTO> enrolledStudents;

        public Course getCourse() {
            return course;
        }

        public void setCourse(Course course) {
            this.course = course;
        }

        public List<StudentClient.StudentDTO> getEnrolledStudents() {
            return enrolledStudents;
        }

        public void setEnrolledStudents(List<StudentClient.StudentDTO> enrolledStudents) {
            this.enrolledStudents = enrolledStudents;
        }
    }
}

