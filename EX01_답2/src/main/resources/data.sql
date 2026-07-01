-- seed 도서 3종 (available_copies 로 대출/재고 시나리오 구성)
INSERT INTO book (title, isbn, total_copies, available_copies) VALUES ('클린 코드', '978-89-0001', 3, 3);
INSERT INTO book (title, isbn, total_copies, available_copies) VALUES ('테스트 주도 개발', '978-89-0002', 2, 0);
INSERT INTO book (title, isbn, total_copies, available_copies) VALUES ('이펙티브 자바', '978-89-0003', 5, 5);
