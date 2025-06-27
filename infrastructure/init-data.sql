-- 캐시노트 마켓 주문 서비스 초기 데이터

-- 데이터베이스 생성 (이미 docker-compose에서 생성됨)
USE vincenzo_shopping;

-- 회원 테이블
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    cashpoint_balance DECIMAL(12,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 상품 테이블
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 주문 테이블
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_member_id (member_id),
    INDEX idx_order_number (order_number)
);

-- 주문 상품 테이블
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 결제 테이블
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_key VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    payment_type VARCHAR(20) NOT NULL, -- SINGLE, COMBINED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_payment_key (payment_key)
);

-- 결제 방법 테이블
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    method_type VARCHAR(20) NOT NULL, -- PG, CASHPOINT, COUPON, BNPL
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    external_transaction_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- 쿠폰 테이블
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    discount_type VARCHAR(20) NOT NULL, -- FIXED, PERCENTAGE
    discount_value DECIMAL(12,2) NOT NULL,
    min_order_amount DECIMAL(12,2) DEFAULT 0.00,
    max_discount_amount DECIMAL(12,2),
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    usage_limit INT DEFAULT 1,
    used_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 회원 쿠폰 테이블
CREATE TABLE IF NOT EXISTS member_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, USED, EXPIRED
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member_coupon (member_id, coupon_id)
);

-- 초기 데이터 삽입

-- 테스트 회원 데이터
INSERT INTO members (username, email, name, cashpoint_balance) VALUES
('test1', 'test1@example.com', '테스트 사용자1', 50000.00),
('test2', 'test2@example.com', '테스트 사용자2', 100000.00),
('seller1', 'seller1@example.com', '판매자1', 0.00);

-- 테스트 상품 데이터
INSERT INTO products (name, description, price, stock_quantity, seller_id) VALUES
('스마트폰', '최신 스마트폰입니다.', 899000.00, 50, 3),
('노트북', '고성능 노트북입니다.', 1500000.00, 30, 3),
('이어폰', '무선 이어폰입니다.', 150000.00, 100, 3),
('마우스', '게이밍 마우스입니다.', 80000.00, 200, 3);

-- 테스트 쿠폰 데이터
INSERT INTO coupons (code, name, discount_type, discount_value, min_order_amount, max_discount_amount, valid_from, valid_until, usage_limit) VALUES
('WELCOME10', '신규회원 10% 할인', 'PERCENTAGE', 10.00, 100000.00, 50000.00, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1),
('FIXED5000', '5000원 고정할인', 'FIXED', 5000.00, 50000.00, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 5),
('VIP20', 'VIP 20% 할인', 'PERCENTAGE', 20.00, 200000.00, 100000.00, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 3);

-- 테스트 회원에게 쿠폰 지급
INSERT INTO member_coupons (member_id, coupon_id) VALUES
(1, 1), -- test1에게 WELCOME10 쿠폰
(1, 2), -- test1에게 FIXED5000 쿠폰
(2, 1), -- test2에게 WELCOME10 쿠폰
(2, 3); -- test2에게 VIP20 쿠폰

-- 인덱스 생성
CREATE INDEX idx_member_coupons_member_id ON member_coupons(member_id);
CREATE INDEX idx_member_coupons_status ON member_coupons(status);
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_members_status ON members(status);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payment_methods_status ON payment_methods(status);

COMMIT;
